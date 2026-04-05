package com.example.socialimpact.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.socialimpact.R
import com.example.socialimpact.ui.activity.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SocialImpactMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        updateTokenInFirestore(token)
    }

    private fun updateTokenInFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("account")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FCM", "Token successfully synced to Firestore for user: $uid")
            }
            .addOnFailureListener { e: Exception ->
                Log.e("FCM", "Failed to sync token to Firestore", e)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        val postId = message.data["postId"]
        val title = message.notification?.title ?: message.data["title"] ?: "New Donation"
        val body = message.notification?.body ?: message.data["body"] ?: "Check your latest donations"

        showNotification(title, body, postId)
    }

    private fun showNotification(title: String?, body: String?, postId: String?) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            postId?.let { putExtra("postId", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "donations_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Social Impact Donations", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for received donations"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
