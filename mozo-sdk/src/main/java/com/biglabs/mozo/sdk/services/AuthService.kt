package com.biglabs.mozo.sdk.services

import com.biglabs.mozo.sdk.utils.logAsError
import java.util.*

class AuthService private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }

    init {
        /* TODO
        check token
        init anonymous user
         */

        UUID.randomUUID().toString().logAsError("uuid")
    }

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