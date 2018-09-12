package com.biglabs.mozo.sdk.services

import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models.AnonymousUserInfo
import com.biglabs.mozo.sdk.core.Models.UserInfo
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AuthService private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }
    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }

    init {
        launch {
            if (!isSignedIn()) {
                val anonymousUser = initAnonymousUser()
                anonymousUser.toString().logAsError()
                // TODO authentication with anonymousUser
            }
        }
    }

    fun signIn() {




        MozoSDK.context?.let {
            launch {
                var user = mozoDB.userInfo().get()
                if (user == null) {
                    user = UserInfo(userId = "abcxyz", phoneNumber = "0123456789", fullName = "Vu Nguyen")
                    mozoDB.userInfo().save(user)

                    wallet.initWallet(user.userId)

                    // TODO claim Mozo token from anonymous to this user
                } else {
                    EventBus.getDefault().register(this@AuthService)
                    SecurityActivity.start(it)
                }
            }
        }
    }

    suspend fun isSignedIn(): Boolean {
        return if (MozoSDK.context != null) {
            mozoDB.userInfo().get() != null
        } else false
    }

    fun signOut() {
        MozoDatabase.destroyInstance()
    }

    @Subscribe
    internal fun onReceivePin(event: MessageEvent.Pin) {
        EventBus.getDefault().unregister(this@AuthService)
        launch(UI) {
            // TODO try to load wallet info from DB by pin
            // verify PIN

            Toast.makeText(MozoSDK.context!!, "receive pin in Auth", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun initAnonymousUser(): AnonymousUserInfo {
        var anonymousUser = mozoDB.anonymousUserInfo().get()
        if (anonymousUser == null) {
            val userId = UUID.randomUUID().toString()
            anonymousUser = AnonymousUserInfo(userId = userId)

            mozoDB.anonymousUserInfo().save(anonymousUser)
        }

        return anonymousUser
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