package com.jarrod.house.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.datastore.DataStoreManager
import com.jarrod.house.data.model.TestNotificationRequest
import com.jarrod.house.utils.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationTestUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val fcmToken: String? = null
)

class NotificationTestViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationTestUiState())
    val uiState: StateFlow<NotificationTestUiState> = _uiState.asStateFlow()

    fun sendTestNotification(context: Context, title: String, body: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            try {
                val response = RetrofitClient.apiService.sendTestNotification(
                    TestNotificationRequest(title, body)
                )
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Test notification sent successfully!",
                        isError = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to send notification: ${response.code()}",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    fun refreshFcmToken(context: Context) {
        viewModelScope.launch {
            try {
                val dataStoreManager = DataStoreManager(context)
                val notificationManager = NotificationManager(context, dataStoreManager)
                
                // Re-initialize FCM to get fresh token
                notificationManager.initializeFirebaseMessaging()
                
                // Get the token from storage
                val token = dataStoreManager.getFcmToken()
                _uiState.value = _uiState.value.copy(
                    fcmToken = token,
                    message = "FCM token refreshed",
                    isError = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Failed to refresh token: ${e.message}",
                    isError = true
                )
            }
        }
    }
    
    fun loadFcmToken(context: Context) {
        viewModelScope.launch {
            try {
                val dataStoreManager = DataStoreManager(context)
                val token = dataStoreManager.getFcmToken()
                _uiState.value = _uiState.value.copy(fcmToken = token)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}