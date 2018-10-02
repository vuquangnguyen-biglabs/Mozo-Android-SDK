package com.biglabs.mozo.sdk.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.FragmentActivity
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.biglabs.mozo.sdk.utils.setMatchParent
import com.biglabs.mozo.sdk.utils.string
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.openid.appauth.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

internal class MozoAuthActivity : FragmentActivity() {

    private var mAuthService: AuthorizationService? = null
    private var mAuthStateManager: AuthStateManager? = null

    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()
    private var mAuthIntentLatch = CountDownLatch(1)

    private var modeSignIn = true
    private var signOutConfiguration: AuthorizationServiceConfiguration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_loading)
        setMatchParent()

        modeSignIn = intent.getBooleanExtra(FLAG_MODE_SIGN_IN, modeSignIn)

        mAuthStateManager = AuthStateManager.getInstance(this)
        if (modeSignIn && mAuthStateManager!!.current.isAuthorized) {
            doResponseAndFinish()
            return
        }

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
        mAuthService?.dispose()
        mAuthService = AuthorizationService(this)
        mAuthRequest.set(null)
        mAuthIntent.set(null)

        if (!modeSignIn) {
            signOutConfiguration = AuthorizationServiceConfiguration(
                    Uri.parse(string(R.string.auth_logout_uri)),
                    Uri.parse(string(R.string.auth_logout_uri)),
                    null
            )
            initializeAuthRequest()
            return
        }

        if (mAuthStateManager!!.current.authorizationServiceConfiguration != null) {
            initializeAuthRequest()
            return
        }

        AuthorizationServiceConfiguration.fetchFromUrl(
                Uri.parse(string(R.string.auth_discovery_uri))
        ) { config, ex ->
            if (config == null) {
                doResponseAndFinish(exception = ex)
                return@fetchFromUrl
            }

            mAuthStateManager!!.replace(AuthState(config))
            initializeAuthRequest()
        }
    }

    private fun initializeAuthRequest() {
        createAuthRequest()
        warmUpBrowser()
        doAuth()
    }

    private fun createAuthRequest() {
        val authRequestBuilder = AuthorizationRequest.Builder(
                if (modeSignIn)
                    mAuthStateManager!!.current.authorizationServiceConfiguration!!
                else
                    signOutConfiguration!!,
                string(R.string.auth_client_id),
                ResponseTypeValues.CODE,
                Uri.parse(string(R.string.auth_redirect_uri, R.string.redirect_url_scheme)))
                .setPrompt("login")
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
        startActivityForResult(intent, KEY_DO_AUTHENTICATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when {
            requestCode == KEY_DO_AUTHENTICATION && modeSignIn -> {
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
            !modeSignIn -> {
                doResponseAndFinish()
            }
            else -> doResponseAndFinish(Exception("No Result"))
        }
    }

    private fun exchangeAuthorizationCode(response: AuthorizationResponse) {
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

    private fun doResponseAndFinish(exception: Exception? = null) {
        launch {
            if (!modeSignIn) {
                signOutCallBack?.invoke()

            } else if (exception == null) {
                MozoAuth.getInstance().saveProfile().await()
            }

            launch(UI) {
                EventBus.getDefault().post(MessageEvent.Auth(modeSignIn, exception))
                finishAndRemoveTask()
            }
        }
    }

    companion object {
        private const val FLAG_MODE_SIGN_IN = "FLAG_MODE_SIGN_IN"
        private const val KEY_DO_AUTHENTICATION = 100

        private var signOutCallBack: (() -> Unit)? = null

        private fun start(context: Context, signIn: Boolean = true) {
            Intent(context, MozoAuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra(FLAG_MODE_SIGN_IN, signIn)
                context.startActivity(this)
            }
        }

        fun signIn(context: Context) {
            start(context)
        }

        fun signOut(context: Context, callback: (() -> Unit)? = null) {
            signOutCallBack = callback
            start(context, signIn = false)
        }
    }
}
