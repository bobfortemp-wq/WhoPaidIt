package com.bob.whopaidit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")
        val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password")
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    val savedEmail: Flow<String> = dataStore.data.map { preferences ->
        preferences[SAVED_EMAIL_KEY] ?: ""
    }

    val savedPassword: Flow<String> = dataStore.data.map { preferences ->
        preferences[SAVED_PASSWORD_KEY] ?: ""
    }

    val rememberMe: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[REMEMBER_ME_KEY] ?: false
    }

    suspend fun saveRememberMe(email: String, password: String, rememberMe: Boolean) {
        dataStore.edit { preferences ->
            if (rememberMe) {
                preferences[SAVED_EMAIL_KEY] = email
                preferences[SAVED_PASSWORD_KEY] = password
                preferences[REMEMBER_ME_KEY] = true
            } else {
                preferences.remove(SAVED_EMAIL_KEY)
                preferences.remove(SAVED_PASSWORD_KEY)
                preferences[REMEMBER_ME_KEY] = false
            }
        }
    }
}
