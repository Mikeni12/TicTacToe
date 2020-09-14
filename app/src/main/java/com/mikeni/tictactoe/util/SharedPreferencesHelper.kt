package com.mikeni.tictactoe.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class SharedPreferencesHelper {

    companion object {
        @Volatile
        private var instance: SharedPreferencesHelper? = null
        private var prefs: SharedPreferences? = null
        private val LOCK = Any()

        private const val NICKNAME = "NICKNAME"
        private const val ID = "ID"

        operator fun invoke(context: Context): SharedPreferencesHelper =
            instance ?: synchronized(LOCK) {
                instance ?: buildHelper(context).also {
                    instance = it
                }
            }

        private fun buildHelper(context: Context): SharedPreferencesHelper {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferencesHelper()
        }
    }

    fun saveUserNickname(nickname: String) {
        prefs?.edit(commit = true) {
            putString(NICKNAME, nickname)
        }
    }

    fun saveUserId(id: String) {
        prefs?.edit(commit = true) {
            putString(ID, id)
        }
    }

    fun getUserNickname() = prefs?.getString(NICKNAME, "") ?: ""

    val nickname = prefs?.getString(NICKNAME, "") ?: ""
    val id = prefs?.getString(ID, "")
}