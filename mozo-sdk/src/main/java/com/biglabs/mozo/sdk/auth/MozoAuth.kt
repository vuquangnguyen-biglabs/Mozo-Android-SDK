package com.biglabs.mozo.sdk.auth

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models.AnonymousUserInfo
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

@Suppress("RedundantSuspendModifier", "unused")
class MozoAuth private constructor() {

    private val wallet: WalletService by lazy { WalletService.getInstance() }
    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }
    private val authStateManager: AuthStateManager by lazy { AuthStateManager.getInstance(MozoSDK.context!!) }

    private var mAuthListener: AuthenticationListener? = null

    init {
        launch {
            val isSignedIn = isSignedIn()
            if (!isSignedIn) {
                val anonymousUser = initAnonymousUser()
                anonymousUser.toString().logAsError()
                // TODO authentication with anonymousUser
            } else {
                val response = MozoApiService.getInstance(MozoSDK.context!!).fetchProfile().await()
                if (response.code() == 401) {
                    signOut()
                    // call https://dev.keycloak.mozocoin.io/auth/realms/mozo/protocol/openid-connect/logout
                    return@launch
                } else {
                    response.body()?.run {
                        mozoDB.profile().save(this)
                    }
                    if (authStateManager.current.needsTokenRefresh) {
                        doRefreshToken()
                    }
                }
            }

            launch(UI) {
                mAuthListener?.onChanged(isSignedIn)
            }
        }
    }

    fun signIn() {
        MozoSDK.context?.run {
            if (!EventBus.getDefault().isRegistered(this@MozoAuth)) {
                EventBus.getDefault().register(this@MozoAuth)
            }
            MozoAuthActivity.start(this)
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

    fun setAuthenticationListener(listener: AuthenticationListener) {
        this.mAuthListener = listener
    }

    @Subscribe
    internal fun onAuthorizeChanged(auth: MessageEvent.Auth) {
        EventBus.getDefault().unregister(this@MozoAuth)

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

    private fun doRefreshToken() {
        try {
            AuthorizationService(MozoSDK.context!!).performTokenRequest(
                    authStateManager.current.createTokenRefreshRequest(),
                    authStateManager.current.clientAuthentication) { response, ex ->
                authStateManager.updateAfterTokenResponse(response, ex)
                response?.run {
                    "Refresh token successful: $accessToken".logAsError()
                }
                ex?.run {
                    "Refresh token failed: $message".logAsError()
                }
            }
        } catch (ex: Exception) {
            "Fail to refresh token: ${ex.message}".logAsError("MozoAuth")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MozoAuth? = null

        fun getInstance(): MozoAuth =
                INSTANCE ?: synchronized(this) {
                    INSTANCE = MozoAuth()
                    INSTANCE!!
                }
    }
}