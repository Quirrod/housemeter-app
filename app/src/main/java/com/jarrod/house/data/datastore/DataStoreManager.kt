package com.jarrod.house.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Single DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object DataStoreKeys {
    val TOKEN_KEY = stringPreferencesKey("auth_token")
    val USER_ID_KEY = stringPreferencesKey("user_id")
    val USERNAME_KEY = stringPreferencesKey("username")
    val ROLE_KEY = stringPreferencesKey("role")
    val APARTMENT_ID_KEY = stringPreferencesKey("apartment_id")
    val HOUSE_ID_KEY = stringPreferencesKey("house_id")
    val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
}

class DataStoreManager(private val context: Context) {
    
    // Auth token methods
    suspend fun getAuthToken(): String? {
        return context.dataStore.data.first()[DataStoreKeys.TOKEN_KEY]
    }
    
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.TOKEN_KEY] = token
        }
    }
    
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.TOKEN_KEY)
        }
    }
    
    // FCM token methods
    suspend fun getFcmToken(): String? {
        return context.dataStore.data.first()[DataStoreKeys.FCM_TOKEN_KEY]
    }
    
    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.FCM_TOKEN_KEY] = token
        }
    }
    
    suspend fun clearFcmToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.FCM_TOKEN_KEY)
        }
    }
    
    // User data methods
    suspend fun saveUserData(
        userId: String,
        username: String,
        role: String,
        apartmentId: String?
    ) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.USER_ID_KEY] = userId
            preferences[DataStoreKeys.USERNAME_KEY] = username
            preferences[DataStoreKeys.ROLE_KEY] = role
            apartmentId?.let { preferences[DataStoreKeys.APARTMENT_ID_KEY] = it }
        }
    }
    
    suspend fun getUserRole(): String? {
        return context.dataStore.data.first()[DataStoreKeys.ROLE_KEY]
    }
    
    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[DataStoreKeys.USER_ID_KEY]
    }
    
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.USER_ID_KEY)
            preferences.remove(DataStoreKeys.USERNAME_KEY)
            preferences.remove(DataStoreKeys.ROLE_KEY)
            preferences.remove(DataStoreKeys.APARTMENT_ID_KEY)
        }
    }
}