package com.example.plantastic.data

import android.content.Context
import android.content.SharedPreferences
import com.example.plantastic.data.model.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

object ChatHistoryRepository {

    private const val PREFS_NAME = "chat_history"
    private const val KEY_MESSAGES = "messages_"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getMessages(context: Context, scanId: String): List<ChatMessage> {
        val json = prefs(context).getString(KEY_MESSAGES + scanId, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                ChatMessage(
                    text = obj.getString("text"),
                    isUser = obj.getBoolean("isUser"),
                    timestamp = obj.getLong("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMessages(context: Context, scanId: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { msg ->
            val obj = JSONObject().apply {
                put("text", msg.text)
                put("isUser", msg.isUser)
                put("timestamp", msg.timestamp)
            }
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_MESSAGES + scanId, array.toString()).apply()
    }

    fun addMessage(context: Context, scanId: String, message: ChatMessage) {
        val current = getMessages(context, scanId).toMutableList()
        current.add(message)
        saveMessages(context, scanId, current)
    }

    fun clearChat(context: Context, scanId: String) {
        prefs(context).edit().remove(KEY_MESSAGES + scanId).apply()
    }
}
