const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Triggered when a donation document is created or updated.
 * Sends a push notification to the post owner.
 */
exports.onDonationWritten = onDocumentWritten("posts/{postId}/donations/{donorId}", async (event) => {
    const postId = event.params.postId;

    // Get the data after the change
    const donationData = event.data.after.data();
    if (!donationData) {
        console.log("Donation deleted, skipping notification.");
        return;
    }

    try {
        console.log(`Processing donation for post: ${postId}`);

        // 1. Get Post details to find the owner
        const postDoc = await db.doc(`posts/${postId}`).get();
        if (!postDoc.exists) {
            console.error(`Post ${postId} not found.`);
            return;
        }
        const post = postDoc.data();
        const ownerId = post.userId;

        // 2. Get Owner's FCM token from the 'account' collection
        const ownerDoc = await db.doc(`account/${ownerId}`).get();
        if (!ownerDoc.exists) {
            console.error(`Owner ${ownerId} account document not found.`);
            return;
        }
        const fcmToken = ownerDoc.data().fcmToken;
        if (!fcmToken) {
            console.warn(`Owner ${ownerId} has no fcmToken stored.`);
            return;
        }

        // 3. Resolve Donor Name
        let donorName = "Someone";
        if (donationData.user_ref) {
            const donorDoc = await donationData.user_ref.get();
            if (donorDoc.exists) {
                donorName = donorDoc.data().fullName || "Someone";
            }
        }

        // 4. Construct Notification Message
        const items = donationData.dynamicNeed || [];
        // Get the most recent item added
        const latestItem = items.length > 0 ? items[items.length - 1] : null;
        const itemDetail = latestItem ? `${latestItem.quantity} ${latestItem.name}` : "items";

        const message = {
            notification: {
                title: "New Donation Received!",
                body: `${donorName} donated ${itemDetail} to your post: ${post.title}`
            },
            data: {
                postId: postId,
                type: "DONATION"
            },
            token: fcmToken,
            android: {
                priority: "high",
                notification: {
                    channelId: "donations_channel"
                }
            }
        };

        // 5. Send Notification
        const response = await admin.messaging().send(message);
        console.log(`Successfully sent notification to owner ${ownerId}. Response: ${response}`);

    } catch (error) {
        console.error("Error processing donation notification:", error);
    }
});

/**
 * STRIPE AND OTHER FUNCTIONS REMAIN UNCHANGED
 */
exports.syncStripeCustomerStatus = onCall({ secrets: ["STRIPE_SECRET_KEY"] }, async (request) => {
    const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
    const uid = request.auth.uid;
    try {
        const sendDoc = await db.collection("account").doc(uid).collection("payment").doc("send").get();
        let customerId = sendDoc.data()?.stripeCustomerId;
        if (!customerId) {
            const customer = await stripe.customers.create({ metadata: { firebaseUid: uid } });
            customerId = customer.id;
        }
        await db.collection("account").doc(uid).collection("payment").doc("send").set({ stripeCustomerId: customerId }, { merge: true });
        return { success: true };
    } catch (error) { throw new HttpsError("internal", error.message); }
});

exports.createDonationIntent = onCall(
    { secrets: ["STRIPE_SECRET_KEY", "STRIPE_PUBLISHABLE_KEY"] },
    async (request) => {
        const stripe = require("stripe")(process.env.STRIPE_SECRET_KEY);
        const { postId, donorId, amountUsd } = request.data;
        try {
            const postDoc = await db.doc(`posts/${postId}`).get();
            const receiverId = postDoc.data().userId;
            const receiverPaymentDoc = await db.doc(`account/${receiverId}/payment/receive`).get();
            const connectedAccountId = receiverPaymentDoc.data().connectedAccountId;
            const paymentIntent = await stripe.paymentIntents.create({
                amount: Math.round(parseFloat(amountUsd) * 100),
                currency: 'usd',
                transfer_data: { destination: connectedAccountId },
                metadata: { postId, donorId, amountUsd }
            });
            return { paymentIntentClientSecret: paymentIntent.client_secret, publishableKey: process.env.STRIPE_PUBLISHABLE_KEY };
        } catch (error) { throw new HttpsError("internal", error.message); }
    }
);
