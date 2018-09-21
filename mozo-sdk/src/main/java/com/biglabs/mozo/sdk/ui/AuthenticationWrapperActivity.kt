package com.biglabs.mozo.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.Toast
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.biglabs.mozo.sdk.utils.logAsError
import com.biglabs.mozo.sdk.utils.setMatchParent
import com.biglabs.mozo.sdk.utils.string
import kotlinx.android.synthetic.main.view_loading.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.openid.appauth.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class AuthenticationWrapperActivity : FragmentActivity() {

    private var mAuthService: AuthorizationService? = null
    private var mAuthStateManager: AuthStateManager? = null

    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()
    private var mAuthIntentLatch = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_loading)
        setMatchParent()
        mAuthStateManager = AuthStateManager.getInstance(this)

        if (mAuthStateManager!!.current.isAuthorized) {
            doResponseAndFinish()
            return
        }

        if (intent.getBooleanExtra(EXTRA_FAILED, false)) {
            finish()
            return
        }

        displayLoading()
        initializeAppAuth()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuthService?.dispose()
    }

    /**
     * Initializes the authorization service configuration if necessary, either from the local
     * static values or by retrieving an OpenID discovery document.
     */
    private fun initializeAppAuth() {
        recreateAuthorizationService()

        if (mAuthStateManager!!.current.authorizationServiceConfiguration != null) {
            initializeAuthRequest()
            return
        }

        displayLoading()

        AuthorizationServiceConfiguration.fetchFromUrl(
                Uri.parse(string(R.string.auth_discovery_uri))
        ) { config, ex -> this.handleConfigurationRetrievalResult(config, ex) }
    }

    private fun initializeAuthRequest() {
        createAuthRequest()
        warmUpBrowser()
        doAuth()
    }

    private fun handleConfigurationRetrievalResult(config: AuthorizationServiceConfiguration?, ex: AuthorizationException?) {
        if (config == null) {
            doResponseAndFinish(exception = ex)
            return
        }

        mAuthStateManager!!.replace(AuthState(config))
        initializeAuthRequest()
    }

    private fun recreateAuthorizationService() {
        mAuthService?.dispose()
        mAuthService = AuthorizationService(this)
        mAuthRequest.set(null)
        mAuthIntent.set(null)
    }

    private fun createAuthRequest() {
        val authRequestBuilder = AuthorizationRequest.Builder(
                mAuthStateManager!!.current.authorizationServiceConfiguration!!,
                string(R.string.auth_client_id),
                ResponseTypeValues.CODE,
                Uri.parse(string(R.string.auth_redirect_uri, R.string.redirect_url_scheme)))
                .setScope("")

        mAuthRequest.set(authRequestBuilder.build())
    }

    private fun warmUpBrowser() {
        mAuthIntentLatch = CountDownLatch(1)
        val intentBuilder = mAuthService!!.createCustomTabsIntentBuilder(mAuthRequest.get().toUri())
        val customTabs = intentBuilder
                .setShowTitle(true)
                .setInstantAppsEnabled(false)
                .build()

        val extras = Bundle()
        extras.putBinder(CustomTabsIntent.EXTRA_SESSION, null)
        extras.putBoolean(CustomTabsIntent.EXTRA_DEFAULT_SHARE_MENU_ITEM, false)
        extras.putParcelableArrayList(CustomTabsIntent.EXTRA_MENU_ITEMS, null)
        customTabs.intent.putExtras(extras)

        mAuthIntent.set(customTabs)
        mAuthIntentLatch.countDown()
    }

    /**
     * Performs the authorization request, using the browser selected in the spinner,
     * and a user-provided `login_hint` if available.
     */
    private fun doAuth() {
        try {
            mAuthIntentLatch.await()
        } catch (ex: InterruptedException) {
        }

        val intent = mAuthService!!.getAuthorizationRequestIntent(mAuthRequest.get(), mAuthIntent.get())
        startActivityForResult(intent, RC_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        } else {
            val response = AuthorizationResponse.fromIntent(data)
            val ex = AuthorizationException.fromIntent(data)

            if (response != null || ex != null) {
                mAuthStateManager!!.updateAfterAuthorization(response, ex)
            }

            when {
                response?.authorizationCode != null -> {
                    // authorization code exchange is required
                    mAuthStateManager!!.updateAfterAuthorization(response, ex)
                    exchangeAuthorizationCode(response)
                }
                else -> {
                    doResponseAndFinish(exception = ex)
                }
            }
        }
    }

    private fun exchangeAuthorizationCode(response: AuthorizationResponse) {
        displayLoading()
        performTokenRequest(response.createTokenExchangeRequest(), AuthorizationService.TokenResponseCallback { tokenResponse, authException ->
            mAuthStateManager!!.updateAfterTokenResponse(tokenResponse, authException)
            doResponseAndFinish(exception = authException)
        })
    }

    private fun performTokenRequest(request: TokenRequest, callback: AuthorizationService.TokenResponseCallback) {
        val clientAuthentication: ClientAuthentication
        try {
            clientAuthentication = mAuthStateManager!!.current.clientAuthentication
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            doResponseAndFinish(exception = ex)
            return
        }

        mAuthService!!.performTokenRequest(request, clientAuthentication, callback)
    }

    private fun refreshAccessToken() {
        displayLoading()
        performTokenRequest(
                mAuthStateManager!!.current.createTokenRefreshRequest(),
                AuthorizationService.TokenResponseCallback { tokenResponse, authException -> this.handleAccessTokenResponse(tokenResponse, authException) })
    }

    private fun handleAccessTokenResponse(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
        mAuthStateManager!!.updateAfterTokenResponse(tokenResponse, authException)
        Toast.makeText(this, "refreshAccessToken DONE", Toast.LENGTH_SHORT).show()
    }

    private fun displayLoading() {
        launch(UI) {
            loading_container.visibility = View.VISIBLE
        }
    }

    private fun doResponseAndFinish(exception: Exception? = null) {
        launch {
            val currentAuth = mAuthStateManager!!.current
            if (exception == null) {
                val response = MozoApiService.create().fetchProfile().await()
                val serverProfile = response.body()

                if (response.isSuccessful && serverProfile != null) {
                    val mozoDB = MozoDatabase.getInstance(this@AuthenticationWrapperActivity)
                    /* save User info first */
                    mozoDB.userInfo().save(Models.UserInfo(
                            userId = serverProfile.userId,
                            accessToken = currentAuth.accessToken,
                            refreshToken = currentAuth.refreshToken
                    ))

                    /* update local profile to match with server profile */
                    mozoDB.profile().save(serverProfile)
                } else {
                    // TODO handle fetch profile error
                    "Failed to fetch user profile!!".logAsError()
                }
            }

            launch(UI) {
                EventBus.getDefault().post(MessageEvent.Auth(currentAuth, exception))
                finish()
            }
        }
    }

    companion object {
        private const val EXTRA_FAILED = "failed"
        private const val RC_AUTH = 100

        fun start(context: Context) {
            val starter = Intent(context, AuthenticationWrapperActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(starter)
        }
    }
}
