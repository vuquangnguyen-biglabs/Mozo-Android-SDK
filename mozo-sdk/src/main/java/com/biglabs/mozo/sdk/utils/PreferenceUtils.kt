package com.biglabs.mozo.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import com.biglabs.mozo.sdk.BuildConfig

internal class PreferenceUtils private constructor(private val sharedPreferences: SharedPreferences) {

    fun getFlag(flag: String) = sharedPreferences.getBoolean(flag, false)

    fun setFlag(flag: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(flag, value).apply()
    }

    companion object {
        const val FLAG_SYNC_WALLET_INFO = "SYNC_WALLET_INFO"

        @Volatile
        private var INSTANCE: PreferenceUtils? = null

        internal fun getInstance(context: Context): PreferenceUtils =
                INSTANCE ?: synchronized(this) {
                    INSTANCE = PreferenceUtils(
                            context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
                    )
                    return INSTANCE!!
                }

    }
}