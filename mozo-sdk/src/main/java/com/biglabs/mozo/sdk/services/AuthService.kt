package com.biglabs.mozo.sdk.services

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.core.Models.UserInfo
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.launch
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
        MozoSDK.context?.let {
            launch {
                val mozoDB = MozoDatabase.getInstance(it)

                var user = mozoDB.userInfo().get()
                if (user == null) {
                    user = UserInfo(userId = "abcxyz", phoneNumber = "0123456789", fullName = "Vu Nguyen")
                    mozoDB.userInfo().save(user)

                    wallet.initWallet(user.userId)
                } else {

                }
            }
        }
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