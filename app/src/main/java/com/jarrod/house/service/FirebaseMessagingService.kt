package com.jarrod.house.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jarrod.house.MainActivity
import com.jarrod.house.R
import com.jarrod.house.data.datastore.DataStoreManager
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class HouseMeterFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.i("FCM_MESSAGE", "==================================")
        Log.i("FCM_MESSAGE", "📩 RECEIVED FCM MESSAGE:")
        Log.i("FCM_MESSAGE", "From: ${remoteMessage.from}")
        Log.i("FCM_MESSAGE", "Message ID: ${remoteMessage.messageId}")
        Log.i("FCM_MESSAGE", "Data: ${remoteMessage.data}")
        
        remoteMessage.notification?.let { notification ->
            Log.i("FCM_MESSAGE", "Title: ${notification.title}")
            Log.i("FCM_MESSAGE", "Body: ${notification.body}")
            Log.i("FCM_MESSAGE", "==================================")
            
            showNotification(
                title = notification.title ?: "HouseMeter",
                body = notification.body ?: "You have a new notification",
                data = remoteMessage.data
            )
        } ?: run {
            Log.w("FCM_MESSAGE", "No notification payload found")
            Log.i("FCM_MESSAGE", "==================================")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("FCM", "==================================")
        Log.i("FCM", "🔄 NEW FCM TOKEN GENERATED:")
        Log.i("FCM", token)
        Log.i("FCM", "==================================")
        
        // Send token to your backend server
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FCM", "Updating FCM token on server...")
                val dataStoreManager = DataStoreManager(this@HouseMeterFirebaseMessagingService)
                val authToken = dataStoreManager.getAuthToken()
                
                if (authToken != null) {
                    val response = RetrofitClient.getApiService(this@HouseMeterFirebaseMessagingService).registerFcmToken(
                        FcmTokenRequest(token)
                    )
                    if (response.isSuccessful) {
                        dataStoreManager.saveFcmToken(token)
                        Log.i("FCM", "✅ FCM token updated successfully on server")
                    } else {
                        Log.w("FCM", "❌ Failed to update FCM token: ${response.code()}")
                    }
                } else {
                    Log.w("FCM", "⚠️ No auth token available for FCM token update")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Exception while updating FCM token on server", e)
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HouseMeter Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for apartment management"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent if needed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "housemeter_notifications"
    }
}