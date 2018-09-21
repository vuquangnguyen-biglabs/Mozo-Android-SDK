package com.biglabs.mozo.sdk.services

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

@Suppress("RedundantSuspendModifier", "unused")
class AuthService private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }
    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }
    private val authStateManager: AuthStateManager by lazy { AuthStateManager.getInstance(MozoSDK.context!!) }

    private var mAuthListener: AuthenticationListener? = null
//    private var mUserInfo: Models.UserInfo? = null

    init {
        launch {
            val isSignedIn = isSignedIn()
            if (!isSignedIn) {
                val anonymousUser = initAnonymousUser()
                anonymousUser.toString().logAsError()
                // TODO authentication with anonymousUser
            }

            launch(UI) {
                mAuthListener?.onChanged(isSignedIn)
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

    fun isSignedIn() = authStateManager.current.isAuthorized

    fun signOut() {
        launch {
            mozoDB.userInfo().delete()

            val currentState = authStateManager.current
            val clearedState = AuthState(currentState.authorizationServiceConfiguration!!)
            if (currentState.lastRegistrationResponse != null) {
                clearedState.update(currentState.lastRegistrationResponse)
            }
            authStateManager.replace(clearedState)

            launch(UI) {
                EventBus.getDefault().post(MessageEvent.Auth(authStateManager.current, null))
                mAuthListener?.onChanged(false)
            }
        }
    }

    @Subscribe
    internal fun onAuthorizeChanged(auth: MessageEvent.Auth) {
        EventBus.getDefault().unregister(this@AuthService)

        auth.authState.accessToken?.logAsError("\n\nAccessToken")
        auth.authState.refreshToken?.logAsError("\n\nRefreshToken")

        /* notify for caller */
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