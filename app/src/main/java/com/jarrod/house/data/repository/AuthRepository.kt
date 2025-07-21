package com.jarrod.house.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.datastore.dataStore
import com.jarrod.house.data.datastore.DataStoreKeys
import com.jarrod.house.data.model.LoginRequest
import com.jarrod.house.data.model.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class AuthRepository(private val context: Context) {

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val apiService = RetrofitClient.getApiService(context)
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    saveUserData(loginResponse)
                }
            }
            response
        } catch (e: Exception) {
            // Fallback to raw response parsing if generic type casting fails
            try {
                val rawResponse = apiService.loginRaw(LoginRequest(username, password))
                val gson = com.google.gson.Gson()
                val loginResponse = gson.fromJson(rawResponse.string(), LoginResponse::class.java)
                saveUserData(loginResponse)
                Response.success(loginResponse)
            } catch (fallbackException: Exception) {
                throw e // Rethrow original exception if fallback fails
            }
        }
    }

    private suspend fun saveUserData(loginResponse: LoginResponse) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.TOKEN_KEY] = loginResponse.token
            preferences[DataStoreKeys.USER_ID_KEY] = loginResponse.user.id.toString()
            preferences[DataStoreKeys.USERNAME_KEY] = loginResponse.user.username
            preferences[DataStoreKeys.ROLE_KEY] = loginResponse.user.role
            loginResponse.user.apartment_id?.let {
                preferences[DataStoreKeys.APARTMENT_ID_KEY] = it.toString()
            }
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getToken(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DataStoreKeys.TOKEN_KEY]
    }

    fun getUserRole(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DataStoreKeys.ROLE_KEY]
    }

    fun getApartmentId(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DataStoreKeys.APARTMENT_ID_KEY]
    }

    fun getUsername(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DataStoreKeys.USERNAME_KEY]
    }

    suspend fun getProfile(): Response<com.jarrod.house.data.api.UserProfile> {
        val apiService = RetrofitClient.getApiService(context)
        return apiService.getProfile()
    }
}