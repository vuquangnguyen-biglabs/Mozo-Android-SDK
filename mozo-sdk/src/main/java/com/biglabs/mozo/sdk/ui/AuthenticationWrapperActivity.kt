package com.biglabs.mozo.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.biglabs.mozo.sdk.utils.string
import kotlinx.android.synthetic.main.view_loading.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.openid.appauth.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class AuthenticationWrapperActivity : Activity() {

    private var mAuthService: AuthorizationService? = null
    private var mAuthStateManager: AuthStateManager? = null

    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()
    private var mAuthIntentLatch = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_loading)
        mAuthStateManager = AuthStateManager.getInstance(this)

        if (mAuthStateManager!!.current.isAuthorized) {
            EventBus.getDefault().post(MessageEvent.Auth(mAuthStateManager!!.current))
            finish()
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
                    EventBus.getDefault().post(MessageEvent.Auth(mAuthStateManager!!.current, response, ex))
                    finish()
                }
            }
        }
    }

    private fun exchangeAuthorizationCode(response: AuthorizationResponse) {
        displayLoading()
        performTokenRequest(response.createTokenExchangeRequest(), AuthorizationService.TokenResponseCallback { tokenResponse, authException ->
            mAuthStateManager!!.updateAfterTokenResponse(tokenResponse, authException)
            EventBus.getDefault().post(MessageEvent.Auth(mAuthStateManager!!.current, exception = authException))
            finish()
        })
    }

    private fun performTokenRequest(request: TokenRequest, callback: AuthorizationService.TokenResponseCallback) {
        val clientAuthentication: ClientAuthentication
        try {
            clientAuthentication = mAuthStateManager!!.current.clientAuthentication
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            EventBus.getDefault().post(MessageEvent.Auth(mAuthStateManager!!.current, exception = ex))
            finish()
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

    @MainThread
    private fun handleConfigurationRetrievalResult(config: AuthorizationServiceConfiguration?, ex: AuthorizationException?) {
        if (config == null) {
            EventBus.getDefault().post(MessageEvent.Auth(mAuthStateManager!!.current, exception = ex))
            finish()
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

    private fun displayLoading() {
        launch(UI) {
            loading_container.visibility = View.VISIBLE
        }
    }

    @MainThread
    private fun initializeAuthRequest() {
        createAuthRequest()
        warmUpBrowser()
        doAuth()
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
        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        mAuthIntent.set(intentBuilder.build())
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

    companion object {
        private const val EXTRA_FAILED = "failed"
        private const val RC_AUTH = 100

        fun start(context: Context) {
            val starter = Intent(context, AuthenticationWrapperActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}
