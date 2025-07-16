package com.jarrod.house.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Single DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object DataStoreKeys {
    val TOKEN_KEY = stringPreferencesKey("auth_token")
    val USER_ID_KEY = stringPreferencesKey("user_id")
    val USERNAME_KEY = stringPreferencesKey("username")
    val ROLE_KEY = stringPreferencesKey("role")
    val APARTMENT_ID_KEY = stringPreferencesKey("apartment_id")
}