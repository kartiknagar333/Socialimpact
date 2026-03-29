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
