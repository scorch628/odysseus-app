package com.odysseus.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "odysseus_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val apiUrlKey = stringPreferencesKey("api_url")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val usernameKey = stringPreferencesKey("username")
    private val passwordKey = stringPreferencesKey("password")

    val apiUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[apiUrlKey] ?: "http://10.0.2.2:5000/v1"
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[apiKeyKey] ?: ""
    }

    val username: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[usernameKey] ?: ""
    }

    val password: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[passwordKey] ?: ""
    }

    suspend fun saveSettings(apiUrl: String, apiKey: String, username: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[apiUrlKey] = apiUrl
            preferences[apiKeyKey] = apiKey
            preferences[usernameKey] = username
            preferences[passwordKey] = password
        }
    }
}
