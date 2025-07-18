package com.jarrod.house.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
                val token = FirebaseMessaging.getInstance().token.await()
                // Store token locally
                dataStoreManager.saveFcmToken(token)
                // Send token to backend
                sendTokenToBackend(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendTokenToBackend(token: String) {
        try {
            val authToken = dataStoreManager.getAuthToken()
            if (authToken != null) {
                val response = RetrofitClient.apiService.registerFcmToken(
                    FcmTokenRequest(token)
                )
                if (response.isSuccessful) {
                    println("FCM token registered successfully")
                } else {
                    println("Failed to register FCM token: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun removeFcmToken() {
        try {
            val authToken = dataStoreManager.getAuthToken()
            if (authToken != null) {
                RetrofitClient.apiService.removeFcmToken()
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