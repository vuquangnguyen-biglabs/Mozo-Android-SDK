package com.biglabs.mozo.sdk.utils

import android.util.Log
import android.view.View
import com.biglabs.mozo.sdk.BuildConfig
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

internal fun String.logAsError(prefix: String? = null) {
    if (BuildConfig.DEBUG) {
        Log.e("MozoSDK", (if (prefix != null) "$prefix: " else "") + this)
    }
}

fun View.onClick(action: suspend () -> Unit) {
    setOnClickListener {
        launch(UI) {
            action()
        }
    }
}