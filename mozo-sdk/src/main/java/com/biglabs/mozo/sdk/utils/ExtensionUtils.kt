package com.biglabs.mozo.sdk.utils

import android.util.Log
import com.biglabs.mozo.sdk.BuildConfig

internal fun String.logAsError(prefix: String? = null) {
    if (BuildConfig.DEBUG) {
        Log.e("MozoSDK", (if (prefix != null) "$prefix: " else "") + this)
    }
}