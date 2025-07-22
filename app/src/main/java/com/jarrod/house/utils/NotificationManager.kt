package com.jarrod.house.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.jarrod.house.data.datastore.DataStoreManager
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationManager(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    fun initializeFirebaseMessaging() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FCM", "Initializing Firebase Messaging...")
                val token = FirebaseMessaging.getInstance().token.await()
                
                // Store token locally
                dataStoreManager.saveFcmToken(token)
                
                // Enhanced logging for testing
                Log.i("FCM", "=================================")
                Log.i("FCM", "FCM TOKEN FOR TESTING:")
                Log.i("FCM", token)
                Log.i("FCM", "=================================")
                Log.d("FCM", "Token length: ${token.length}")
                Log.d("FCM", "Token saved to DataStore successfully")
                
                // Send token to backend
                sendTokenToBackend(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to initialize Firebase Messaging", e)
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendTokenToBackend(token: String) {
        try {
            Log.d("FCM", "Sending FCM token to backend...")
            val authToken = dataStoreManager.getAuthToken()
            if (authToken != null) {
                Log.d("FCM", "Auth token available, registering FCM token...")
                val response = RetrofitClient.getApiService(context).registerFcmToken(
                    FcmTokenRequest(token)
                )
                if (response.isSuccessful) {
                    Log.i("FCM", "✅ FCM token registered successfully with backend")
                    Log.d("FCM", "Response: ${response.body()}")
                } else {
                    Log.w("FCM", "❌ Failed to register FCM token: ${response.code()}")
                    Log.w("FCM", "Error body: ${response.errorBody()?.string()}")
                }
            } else {
                Log.w("FCM", "⚠️ No auth token available, skipping FCM token registration")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Exception while sending FCM token to backend", e)
            e.printStackTrace()
        }
    }
    
    suspend fun removeFcmToken() {
        try {
            val authToken = dataStoreManager.getAuthToken()
            if (authToken != null) {
                RetrofitClient.getApiService(context).removeFcmToken()
                dataStoreManager.clearFcmToken()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}