package com.biglabs.mozo.sdk.services

import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.auth.AuthenticationListener
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models.AnonymousUserInfo
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.ui.AuthenticationWrapperActivity
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.openid.appauth.AuthState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AuthService private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }
    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }

    private var mAuthListener: AuthenticationListener? = null

    init {
        launch {
            val isSigned = isSignedIn()
            if (!isSigned) {
                val anonymousUser = initAnonymousUser()
                anonymousUser.toString().logAsError()
                // TODO authentication with anonymousUser
            }

            launch(UI) {
                mAuthListener?.onChanged(isSigned)
            }
        }
    }

    fun signIn() {
        MozoSDK.context?.run {
            if (!EventBus.getDefault().isRegistered(this@AuthService)) {
                EventBus.getDefault().register(this@AuthService)
            }
            AuthenticationWrapperActivity.start(this)
            return
        }
    }

    private fun isSignedIn(): Boolean {
        return if (MozoSDK.context != null) {
            mozoDB.userInfo().get() != null
        } else false
    }

    fun signOut() {
        // TODO delete userInfo first
        MozoDatabase.destroyInstance()

        MozoSDK.context?.let {
            val mStateManager = AuthStateManager.getInstance(it)
            val currentState = mStateManager.current
            val clearedState = AuthState(currentState.authorizationServiceConfiguration!!)
            if (currentState.lastRegistrationResponse != null) {
                clearedState.update(currentState.lastRegistrationResponse)
            }
            mStateManager.replace(clearedState)
        }

        mAuthListener?.onChanged(false)
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

    @Subscribe
    internal fun onAuthorized(auth: MessageEvent.Auth) {
        EventBus.getDefault().unregister(this@AuthService)
        auth.authState.accessToken?.logAsError("\n\nAccessToken")
        auth.authState.refreshToken?.logAsError("\n\nRefreshToken")

        mAuthListener?.onChanged(true)

        wallet.initWallet()
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

    fun setAuthenticationListener(listener: AuthenticationListener) {
        this.mAuthListener = listener
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthService? = null

        internal fun getInstance(): AuthService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE = AuthService()
                    INSTANCE!!
                }
    }
}