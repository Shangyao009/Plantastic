package com.example.plantastic.data

import android.content.Context
import android.content.SharedPreferences

object UserPreferencesRepository {

    private const val PREFS = "user_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_AVATAR_URI = "avatar_uri"
    private const val DEFAULT_USERNAME = "Plant User"

    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences ?: throw IllegalStateException(
            "UserPreferencesRepository not initialized. Call init(context) first."
        )
    }

    fun getUsername(): String {
        return getPrefs().getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
    }

    fun setUsername(name: String) {
        getPrefs().edit().putString(KEY_USERNAME, name).apply()
    }

    fun getAvatarUri(): String? {
        return getPrefs().getString(KEY_AVATAR_URI, null)
    }

    fun setAvatarUri(uri: String?) {
        getPrefs().edit().putString(KEY_AVATAR_URI, uri).apply()
    }
}
