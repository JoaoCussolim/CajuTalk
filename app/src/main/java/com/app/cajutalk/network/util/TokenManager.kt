package com.app.cajutalk.network.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.apply()
    }

    fun getAccessToken(context: Context): String? {
        return getPreferences(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return getPreferences(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_ACCESS_TOKEN)
        editor.remove(KEY_REFRESH_TOKEN)
        editor.apply()
    }
}