package com.codelabs.gardahijau.model.local.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PREFS_NAME = "user_prefs"
private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

class UserPreferences @Inject constructor(private val context: Context) {

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }

    fun getAuthToken(): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_AUTH_TOKEN] }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}