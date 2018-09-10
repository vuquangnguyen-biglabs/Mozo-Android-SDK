package com.biglabs.mozo.sdk.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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

fun Context.hideSotfKeyboard(view: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE))?.run {
        (this as InputMethodManager).hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}