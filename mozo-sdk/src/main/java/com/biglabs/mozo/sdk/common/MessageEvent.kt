package com.biglabs.mozo.sdk.common

import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationResponse

object MessageEvent {
    class Pin(val pin: String)
    class Auth(val authState: AuthState, val exception: Exception? = null)
}