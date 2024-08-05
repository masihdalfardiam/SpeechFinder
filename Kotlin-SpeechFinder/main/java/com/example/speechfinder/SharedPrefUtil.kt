package com.example.speechfinder

import android.content.Context
import android.content.SharedPreferences

object SharedPrefUtil {
    private const val PREF_NAME = "speech_finder_prefs"
    private const val TOKEN = "token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(TOKEN, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(TOKEN, null)
    }
}
