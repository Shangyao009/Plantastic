package com.example.plantastic.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for storing and retrieving app settings.
 * Uses SharedPreferences for persistent storage.
 */
object SettingsRepository {

    private const val PREFS_NAME = "plantastic_settings"

    // Keys
    private const val KEY_API_KEY = "api_key"
    private const val KEY_API_BASE_URL = "api_base_url"
    private const val KEY_API_MODEL = "api_model"
    private const val KEY_USE_CUSTOM_API = "use_custom_api"

    // Default values
    private const val DEFAULT_BASE_URL = "https://api.openai.com/"
    private const val DEFAULT_MODEL = "gpt-4o"

    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences ?: throw IllegalStateException(
            "SettingsRepository not initialized. Call init(context) first."
        )
    }

    // API Key
    var apiKey: String
        get() = getPrefs().getString(KEY_API_KEY, "") ?: ""
        set(value) = getPrefs().edit().putString(KEY_API_KEY, value).apply()

    // Base URL
    var apiBaseUrl: String
        get() = getPrefs().getString(KEY_API_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = getPrefs().edit().putString(KEY_API_BASE_URL, value).apply()

    // Model
    var apiModel: String
        get() = getPrefs().getString(KEY_API_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = getPrefs().edit().putString(KEY_API_MODEL, value).apply()

    // Use custom API (vs build config)
    var useCustomApi: Boolean
        get() = getPrefs().getBoolean(KEY_USE_CUSTOM_API, false)
        set(value) = getPrefs().edit().putBoolean(KEY_USE_CUSTOM_API, value).apply()

    // Check if custom API is configured
    fun isCustomApiConfigured(): Boolean {
        return useCustomApi && apiKey.isNotBlank()
    }

    // Clear all settings
    fun clearAll() {
        getPrefs().edit().clear().apply()
    }
}
