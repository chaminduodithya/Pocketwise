package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finance_prefs")

data class UserPrefs(
    val bankBalance: Double = 142500.0,
    val bankName: String = "Commercial Bank",
    val userName: String = "Kasun",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isSeeded: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val BANK_BALANCE = doublePreferencesKey("bank_balance")
        val BANK_NAME = stringPreferencesKey("bank_name")
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_UPDATED = longPreferencesKey("last_updated")
        val IS_SEEDED = booleanPreferencesKey("is_seeded")
    }

    val userPrefs: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        UserPrefs(
            bankBalance = prefs[Keys.BANK_BALANCE] ?: 142500.0,
            bankName = prefs[Keys.BANK_NAME] ?: "Commercial Bank",
            userName = prefs[Keys.USER_NAME] ?: "Kasun",
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.LIGHT.name)
            }.getOrDefault(ThemeMode.LIGHT),
            lastUpdatedMillis = prefs[Keys.LAST_UPDATED] ?: System.currentTimeMillis(),
            isSeeded = prefs[Keys.IS_SEEDED] ?: false
        )
    }

    suspend fun getPrefsOnce(): UserPrefs = userPrefs.first()

    suspend fun setBankBalance(newBalance: Double, bankName: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BANK_BALANCE] = newBalance
            if (!bankName.isNullOrBlank()) {
                prefs[Keys.BANK_NAME] = bankName.trim()
            }
            prefs[Keys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    suspend fun adjustBankBalance(delta: Double) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BANK_BALANCE] ?: 0.0
            prefs[Keys.BANK_BALANCE] = (current + delta).coerceAtLeast(0.0)
            prefs[Keys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    suspend fun setUserProfile(userName: String, bankName: String) {
        context.dataStore.edit { prefs ->
            if (userName.isNotBlank()) prefs[Keys.USER_NAME] = userName.trim()
            if (bankName.isNotBlank()) prefs[Keys.BANK_NAME] = bankName.trim()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun markSeeded() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SEEDED] = true
            if (prefs[Keys.BANK_BALANCE] == null) {
                prefs[Keys.BANK_BALANCE] = 142500.0
            }
            prefs[Keys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }
}
