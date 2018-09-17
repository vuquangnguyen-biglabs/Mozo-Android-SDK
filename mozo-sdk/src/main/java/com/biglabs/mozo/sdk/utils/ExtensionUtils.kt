package com.biglabs.mozo.sdk.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.support.annotation.StringRes
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.biglabs.mozo.sdk.BuildConfig
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

internal fun Activity.setMatchParent() {
    val attrs = window.attributes
    attrs.width = ViewGroup.LayoutParams.MATCH_PARENT
    attrs.height = ViewGroup.LayoutParams.MATCH_PARENT
    window.attributes = attrs
}

internal fun String.logAsError(prefix: String? = null) {
    if (BuildConfig.DEBUG) {
        Log.e("MozoSDK", (if (prefix != null) "$prefix: " else "") + this)
    }
}

internal fun View.onClick(action: suspend () -> Unit) {
    setOnClickListener {
        launch(UI) {
            action()
        }
    }
}

internal fun Context.hideSoftKeyboard(view: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE))?.run {
        (this as InputMethodManager).hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}

internal fun Context.showSoftKeyboard(view: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE))?.run {
        (this as InputMethodManager).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

internal fun Context.string(@StringRes id: Int, @StringRes idRef: Int = 0): String {
    return if (idRef != 0) getString(id, string(idRef)) else getString(id)
}

internal fun Resources.dp2Px(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics)
}