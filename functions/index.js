const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Helper to sync Customer data (Senders) to Firestore path: account/{uid}/payment/send
 */
async function syncCustomerData(customerId, stripe) {
    try {
        console.log(`[Sync] Fetching latest data for Customer: ${customerId}`);
        const customer = await stripe.customers.retrieve(customerId);

        let userId = customer.metadata.firebaseUid;

        // Fallback: If metadata is missing, find user by customerId in Firestore
        if (!userId) {
            console.log(`[Sync] No metadata found. Searching Firestore for customer ${customerId}...`);
            const snap = await db.collectionGroup("payment")
                .where("stripeCustomerId", "==", customerId)
                .limit(1)
                .get();

            if (!snap.empty) {
                // Path is account/{uid}/payment/send -> parent.parent gets the uid doc
                userId = snap.docs[0].ref.parent.parent.id;
                console.log(`[Sync] Found userId ${userId} via Firestore lookup.`);
                // Repair metadata in Stripe
                await stripe.customers.update(customerId, { metadata: { firebaseUid: userId } });
            } else {
                console.error(`[Sync] Could not find any Firebase user for customer ${customerId}`);
                return;
            }
        }

        const paymentMethods = await stripe.paymentMethods.list({
            customer: customerId,
            type: 'card',
        });

        const hasSavedCard = paymentMethods.data.length > 0;
        console.log(`[Sync] User ${userId} has ${paymentMethods.data.length} saved cards.`);

        const updateData = {
            stripeCustomerId: customerId,
            hasSavedPaymentMethod: hasSavedCard,
            status: hasSavedCard ? "active" : "setup_required",
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        };

        if (hasSavedCard) {
            const card = paymentMethods.data[0].card;
            updateData.defaultCardBrand = card.brand;
            updateData.defaultCardLast4 = card.last4;
        } else {
            updateData.defaultCardBrand = null;
            updateData.defaultCardLast4 = null;
        }

        await db.collection("account").doc(userId).collection("payment").doc("send").set(updateData, { merge: true });
        console.log(`[Sync] Firestore updated for: account/${userId}/payment/send`);
    } catch (e) {
        console.error("[Sync] Error:", e);
    }
}

/**
 * UNIFIED WEBHOOK
 */
exports.stripeWebhook = onRequest(
  { secrets: ["STRIPE_SECRET_KEY", "STRIPE_WEBHOOK_SECRET"] },
  async (req, res) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const sig = req.headers["stripe-signature"];
    let event;

    try {
      event = stripe.webhooks.constructEvent(req.rawBody, sig, process.env.STRIPE_WEBHOOK_SECRET);
    } catch (err) {
      console.error(`[Webhook] Signature Error: ${err.message}`);
      return res.status(400).send(`Webhook Error: ${err.message}`);
    }

    console.log(`[Webhook] Event: ${event.type}`);

    try {
        if (event.type === "account.updated") {
            const account = event.data.object;
            const userId = account.metadata.firebaseUid;
            if (userId) {
                const onboardingComplete = account.details_submitted && account.charges_enabled;
                await db.collection("account").doc(userId).collection("payment").doc("receive").set({
                    connectedAccountId: account.id,
                    onboardingComplete: onboardingComplete,
                    chargesEnabled: account.charges_enabled,
                    payoutsEnabled: account.payouts_enabled,
                    status: onboardingComplete ? "active" : "pending",
                    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                }, { merge: true });
            }
        }

        if (event.type === "payment_intent.succeeded") {
            const intent = event.data.object;
            const { postId, donorId, donorUserRef, amountUsd } = intent.metadata;

            if (postId && donorId) {
                console.log(`[Webhook] Payment Succeeded for Post: ${postId}, Donor: ${donorId}`);

                const now = admin.firestore.FieldValue.serverTimestamp();
                const donationItem = {
                    ispending: false,
                    name: "Fund",
                    quantity: amountUsd,
                    timestamp: now,
                    paymentIntentId: intent.id
                };

                const donationRef = db.doc(`posts/${postId}/donations/${donorId}`);
                const postRef = db.doc(`posts/${postId}`);

                await db.runTransaction(async (t) => {
                    // 1. Update Donation Record
                    const donationDoc = await t.get(donationRef);
                    if (donationDoc.exists) {
                        t.update(donationRef, {
                            dynamicNeed: admin.firestore.FieldValue.arrayUnion(donationItem),
                            last_donated: now
                        });
                    } else {
                        t.set(donationRef, {
                            user_ref: db.doc(donorUserRef),
                            last_donated: now,
                            dynamicNeed: [donationItem]
                        });
                    }

                    // 2. Update Post Aggregate (Optional but good for UI)
                    const postDoc = await t.get(postRef);
                    if (postDoc.exists) {
                        const dynamicNeeds = postDoc.data().dynamicNeeds || [];
                        const updatedNeeds = dynamicNeeds.map(need => {
                            if (need.name === "Fund") {
                                const current = parseFloat(need.received || "0");
                                return { ...need, received: (current + parseFloat(amountUsd)).toString() };
                            }
                            return need;
                        });
                        t.update(postRef, { dynamicNeeds: updatedNeeds });
                    }
                });
            }
        }

        const customerEvents = ["customer.updated", "payment_method.attached", "payment_method.detached", "setup_intent.succeeded"];
        if (customerEvents.includes(event.type)) {
            const obj = event.data.object;
            const customerId = (event.type === "customer.updated") ? obj.id : obj.customer;
            if (customerId) {
                await syncCustomerData(customerId, stripe);
            }
        }
    } catch (err) {
        console.error("[Webhook] Processing Error:", err);
    }

    res.json({ received: true });
  }
);

exports.createDonationIntent = onCall(
    { secrets: ["STRIPE_SECRET_KEY", "STRIPE_PUBLISHABLE_KEY"] },
    async (request) => {
        const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
        const auth = request.auth;
        if (!auth) throw new HttpsError("unauthenticated", "Auth required.");

        const { postId, donorId, amountUsd } = request.data;
        if (!postId || !donorId || !amountUsd) throw new HttpsError("invalid-argument", "Missing params.");
        if (auth.uid !== donorId) throw new HttpsError("permission-denied", "UID mismatch.");

        try {
            // 1. Validate Post & Owner
            const postDoc = await db.doc(`posts/${postId}`).get();
            if (!postDoc.exists) throw new HttpsError("not-found", "Post not found.");
            const postData = postDoc.data();
            const receiverId = postData.userId;
            if (receiverId === donorId) throw new HttpsError("failed-precondition", "Cannot donate to yourself.");

            // 2. Build donor user_ref
            const donorDoc = await db.doc(`account/${donorId}`).get();
            const donorData = donorDoc.data();
            const donorUserRefPath = `profile/${donorData.type.toLowerCase()}/${donorData.fullName.trim()}/${donorId}`;

            // 3. Check Receiver Stripe Setup
            const receiverPaymentDoc = await db.doc(`account/${receiverId}/payment/receive`).get();
            if (!receiverPaymentDoc.exists || receiverPaymentDoc.data().status !== "active") {
                throw new HttpsError("failed-precondition", "Receiver not ready for payments.");
            }
            const connectedAccountId = receiverPaymentDoc.data().connectedAccountId;

            // 4. Create Ephemeral Key & Customer if needed
            const sendDoc = await db.doc(`account/${donorId}/payment/send`).get();
            let customerId = sendDoc.exists ? sendDoc.data().stripeCustomerId : null;
            if (!customerId) {
                const customer = await stripe.customers.create({
                    email: auth.token.email || "",
                    metadata: { firebaseUid: donorId }
                });
                customerId = customer.id;
                await db.doc(`account/${donorId}/payment/send`).set({ stripeCustomerId: customerId }, { merge: true });
            }

            const ephemeralKey = await stripe.ephemeralKeys.create(
                { customer: customerId },
                { apiVersion: '2022-11-15' }
            );

            // 5. Create PaymentIntent (Destination Charge)
            const amountCents = Math.round(parseFloat(amountUsd) * 100);
            const paymentIntent = await stripe.paymentIntents.create({
                amount: amountCents,
                currency: 'usd',
                customer: customerId,
                automatic_payment_methods: { enabled: true },
                transfer_data: { destination: connectedAccountId },
                metadata: {
                    postId: postId,
                    donorId: donorId,
                    donorUserRef: donorUserRefPath,
                    amountUsd: amountUsd
                }
            });

            return {
                paymentIntentClientSecret: paymentIntent.client_secret,
                ephemeralKeySecret: ephemeralKey.secret,
                customerId: customerId,
                publishableKey: process.env.STRIPE_PUBLISHABLE_KEY // Taken from Firebase Secrets
            };

        } catch (error) {
            console.error("[DonationIntent] Error:", error);
            if (error instanceof HttpsError) throw error;
            throw new HttpsError("internal", error.message);
        }
    }
);

exports.syncStripeCustomerStatus = onCall(
  { secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const auth = request.auth;
    if (!auth) throw new HttpsError("unauthenticated", "Auth required.");
    const uid = auth.uid;

    try {
      const sendRef = db.collection("account").doc(uid).collection("payment").doc("send");
      const sendDoc = await sendRef.get();
      let customerId = sendDoc.exists ? sendDoc.data().stripeCustomerId : null;

      if (!customerId) {
        const customer = await stripe.customers.create({
          email: auth.token.email || "",
          metadata: { firebaseUid: uid }
        });
        customerId = customer.id;
      } else {
        await stripe.customers.update(customerId, { metadata: { firebaseUid: uid } });
      }

      await syncCustomerData(customerId, stripe);
      return { success: true };
    } catch (error) {
        throw new HttpsError("internal", error.message);
    }
  }
);

// Existing Portal, Onboarding, and Dashboard functions remain here...
exports.createStripePortalSession = onCall(
  { secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const auth = request.auth;
    if (!auth) throw new HttpsError("unauthenticated", "Auth required.");
    try {
      const sendDoc = await db.collection("account").doc(auth.uid).collection("payment").doc("send").get();
      const customerId = sendDoc.data()?.stripeCustomerId;
      if (!customerId) throw new HttpsError("not-found", "Customer not found");
      const session = await stripe.billingPortal.sessions.create({
        customer: customerId,
        return_url: `https://${process.env.GCLOUD_PROJECT}.web.app/stripe/return`,
      });
      return { url: session.url };
    } catch (error) {
      throw new HttpsError("internal", error.message);
    }
  }
);

exports.createOrGetOnboardingLink = onCall(
  { secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const auth = request.auth;
    if (!auth) throw new HttpsError("unauthenticated", "Auth required.");
    const uid = auth.uid;
    const projectId = process.env.GCLOUD_PROJECT;
    try {
      const receiveRef = db.collection("account").doc(uid).collection("payment").doc("receive");
      const receiveDoc = await receiveRef.get();
      let connectedAccountId = receiveDoc.exists ? receiveDoc.data().connectedAccountId : null;
      if (!connectedAccountId) {
        const account = await stripe.accounts.create({
          type: "express",
          metadata: { firebaseUid: uid },
          capabilities: { card_payments: { requested: true }, transfers: { requested: true } },
        });
        connectedAccountId = account.id;
        await receiveRef.set({
          connectedAccountId: connectedAccountId,
          onboardingComplete: false,
          chargesEnabled: false,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, { merge: true });
      }
      const accountLink = await stripe.accountLinks.create({
        account: connectedAccountId,
        refresh_url: `https://${projectId}.web.app/stripe/refresh`,
        return_url: `https://${projectId}.web.app/stripe/return`,
        type: "account_onboarding",
      });
      return { url: accountLink.url };
    } catch (error) {
      throw new HttpsError("internal", error.message);
    }
  }
);

exports.createStripeDashboardLink = onCall(
  { secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const auth = request.auth;
    if (!auth) throw new HttpsError("unauthenticated", "Auth required.");
    try {
      const receiveDoc = await db.collection("account").doc(auth.uid).collection("payment").doc("receive").get();
      const loginLink = await stripe.accounts.createLoginLink(receiveDoc.data().connectedAccountId);
      return { url: loginLink.url };
    } catch (error) {
      throw new HttpsError("internal", error.message);
    }
  }
);

exports.syncStripeAccountStatus = onCall(
  { secrets: ["STRIPE_SECRET_KEY"] },
  async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const auth = request.auth;
    if (!auth) throw new HttpsError("unauthenticated", "Auth required.");
    const uid = auth.uid;
    try {
      const receiveRef = db.collection("account").doc(uid).collection("payment").doc("receive");
      const receiveDoc = await receiveRef.get();
      let connectedAccountId = receiveDoc.exists ? receiveDoc.data().connectedAccountId : null;
      if (!connectedAccountId) return { success: false };
      const account = await stripe.accounts.retrieve(connectedAccountId);
      const onboardingComplete = account.details_submitted && account.charges_enabled;
      await receiveRef.set({
        onboardingComplete: onboardingComplete,
        chargesEnabled: account.charges_enabled,
        payoutsEnabled: account.payouts_enabled,
        status: onboardingComplete ? "active" : "pending",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });
      return { success: true };
    } catch (error) {
      throw new HttpsError("internal", error.message);
    }
  }
);
