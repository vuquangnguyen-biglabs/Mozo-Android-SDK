package com.biglabs.mozo.sdk.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.support.annotation.IntegerRes
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

/**
 * Extension method to show a keyboard for View.
 */
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

/**
 * Try to hide the keyboard and returns whether it worked
 * https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
 */
fun View.hideKeyboard(): Boolean {
    try {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (ignored: RuntimeException) { }
    return false
}

/**
 * Set an onclick listener
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener { block(it as T) }

internal fun Context.string(@StringRes id: Int, @StringRes idRef: Int = 0): String {
    return if (idRef != 0) getString(id, string(idRef)) else getString(id)
}

/**
 * Extension method to Get Integer resource for Context.
 */
fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)

internal fun Resources.dp2Px(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics)
}