package com.jarrod.house.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jarrod.house.data.api.RetrofitClient
import com.jarrod.house.data.model.LoginRequest
import com.jarrod.house.data.model.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepository(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val APARTMENT_ID_KEY = stringPreferencesKey("apartment_id")

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val response = RetrofitClient.apiService.login(LoginRequest(username, password))
        if (response.isSuccessful) {
            response.body()?.let { loginResponse ->
                saveUserData(loginResponse)
            }
        }
        return response
    }

    private suspend fun saveUserData(loginResponse: LoginResponse) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = loginResponse.token
            preferences[USER_ID_KEY] = loginResponse.user.id.toString()
            preferences[USERNAME_KEY] = loginResponse.user.username
            preferences[ROLE_KEY] = loginResponse.user.role
            loginResponse.user.apartment_id?.let {
                preferences[APARTMENT_ID_KEY] = it.toString()
            }
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getToken(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    fun getUserRole(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    fun getApartmentId(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APARTMENT_ID_KEY]
    }

    fun getUsername(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }
}