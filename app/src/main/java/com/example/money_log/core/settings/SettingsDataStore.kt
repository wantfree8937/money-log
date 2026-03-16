package com.example.money_log.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 앱의 설정을 관리하는 DataStore 클래스
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        val START_DAY = intPreferencesKey("start_day")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        val LANGUAGE = stringPreferencesKey("language") // "ko", "en"
        val CUSTOM_CATEGORIES = stringSetPreferencesKey("custom_categories")
    }

    // 월 시작일 (기본값: 1)
    val startDay: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[START_DAY] ?: 1
    }

    // 자동 저장 여부 (기본값: false)
    val autoSave: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SAVE] ?: false
    }

    // 다크모드 설정 (기본값: system)
    val darkMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: "system"
    }

    // 언어 설정 (기본값: ko)
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "ko"
    }

    suspend fun updateStartDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[START_DAY] = day
        }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE] = enabled
        }
    }

    suspend fun updateDarkMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = mode
        }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }
}
