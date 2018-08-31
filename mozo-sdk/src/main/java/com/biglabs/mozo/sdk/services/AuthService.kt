package com.biglabs.mozo.sdk.services

class AuthService private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }

    fun signIn() {
        wallet.createWallet()
    }

    fun signOut() {

    }

    companion object {
        @Volatile
        private var INSTANCE: AuthService? = null

        internal fun getInstance(): AuthService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: AuthService()
                }
    }
}