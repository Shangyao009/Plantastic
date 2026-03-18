package com.example.plantastic.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for storing and retrieving app settings.
 * Uses SharedPreferences for persistent storage.
 */
object SettingsRepository {

    private const val PREFS_NAME = "plantastic_settings"

    // Keys - Detection API
    private const val KEY_DETECTION_API_KEY = "detection_api_key"
    private const val KEY_DETECTION_API_BASE_URL = "detection_api_base_url"
    private const val KEY_DETECTION_API_MODEL = "detection_api_model"

    // Keys - Chat API
    private const val KEY_CHAT_API_KEY = "chat_api_key"
    private const val KEY_CHAT_API_BASE_URL = "chat_api_base_url"
    private const val KEY_CHAT_API_MODEL = "chat_api_model"

    // Keys - Settings
    private const val KEY_USE_SEPARATE_APIS = "use_separate_apis"

    // Default values
    private const val DEFAULT_BASE_URL = "https://api.openai.com/"
    private const val DEFAULT_DETECTION_MODEL = "gpt-4o"
    private const val DEFAULT_CHAT_MODEL = "gpt-4o"

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

    // ========== Detection API Settings ==========

    // Detection API Key
    var detectionApiKey: String
        get() = getPrefs().getString(KEY_DETECTION_API_KEY, "") ?: ""
        set(value) = getPrefs().edit().putString(KEY_DETECTION_API_KEY, value).apply()

    // Detection Base URL
    var detectionApiBaseUrl: String
        get() = getPrefs().getString(KEY_DETECTION_API_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = getPrefs().edit().putString(KEY_DETECTION_API_BASE_URL, value).apply()

    // Detection Model
    var detectionApiModel: String
        get() = getPrefs().getString(KEY_DETECTION_API_MODEL, DEFAULT_DETECTION_MODEL) ?: DEFAULT_DETECTION_MODEL
        set(value) = getPrefs().edit().putString(KEY_DETECTION_API_MODEL, value).apply()

    // ========== Chat API Settings ==========

    // Chat API Key
    var chatApiKey: String
        get() = getPrefs().getString(KEY_CHAT_API_KEY, "") ?: ""
        set(value) = getPrefs().edit().putString(KEY_CHAT_API_KEY, value).apply()

    // Chat Base URL
    var chatApiBaseUrl: String
        get() = getPrefs().getString(KEY_CHAT_API_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = getPrefs().edit().putString(KEY_CHAT_API_BASE_URL, value).apply()

    // Chat Model
    var chatApiModel: String
        get() = getPrefs().getString(KEY_CHAT_API_MODEL, DEFAULT_CHAT_MODEL) ?: DEFAULT_CHAT_MODEL
        set(value) = getPrefs().edit().putString(KEY_CHAT_API_MODEL, value).apply()

    // ========== General Settings ==========

    // Use separate APIs (vs single unified API)
    var useSeparateApis: Boolean
        get() = getPrefs().getBoolean(KEY_USE_SEPARATE_APIS, false)
        set(value) = getPrefs().edit().putBoolean(KEY_USE_SEPARATE_APIS, value).apply()

    // Legacy support - single API key
    var apiKey: String
        get() = getPrefs().getString("api_key", "") ?: ""
        set(value) = getPrefs().edit().putString("api_key", value).apply()

    var apiBaseUrl: String
        get() = getPrefs().getString("api_base_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = getPrefs().edit().putString("api_base_url", value).apply()

    var apiModel: String
        get() = getPrefs().getString("api_model", DEFAULT_DETECTION_MODEL) ?: DEFAULT_DETECTION_MODEL
        set(value) = getPrefs().edit().putString("api_model", value).apply()

    var useCustomApi: Boolean
        get() = getPrefs().getBoolean("use_custom_api", false)
        set(value) = getPrefs().edit().putBoolean("use_custom_api", value).apply()

    // Check if any API is configured
    fun isCustomApiConfigured(): Boolean {
        return useCustomApi && apiKey.isNotBlank()
    }

    fun isDetectionApiConfigured(): Boolean {
        return useSeparateApis && detectionApiKey.isNotBlank()
    }

    fun isChatApiConfigured(): Boolean {
        return useSeparateApis && chatApiKey.isNotBlank()
    }

    // Clear all settings
    fun clearAll() {
        getPrefs().edit().clear().apply()
    }
}
