package com.biglabs.mozo.sdk.services

import android.content.Context

class AuthService private constructor(context: Context) {

    fun signIn(context: Context) {
        val wallet: WalletService = WalletService.getInstance(context)
    }

    fun signOut() {

    }

    companion object {
        @Volatile
        private var instance: AuthService? = null

        @Synchronized
        internal fun getInstance(context: Context): AuthService {
            if (instance == null) {
                instance = AuthService(context)
            }
            return instance as AuthService
        }
    }
}