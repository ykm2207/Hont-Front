package com.hont.app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object TokenManager {

    private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    fun getAccessToken(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }

    fun getRefreshToken(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }

    suspend fun clearTokens(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
