package com.biglabs.mozo.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class KeyboardAlwaysEditText : EditText {
    private var onBackPress: (suspend () -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, def: Int) : super(context, attributes, def)

    fun setOnBackPress(action: suspend () -> Unit) {
        this.onBackPress = action
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPress?.let {
                launch(UI) {
                    it()
                }
            }
            return true
        }
        return false
    }
}

fun KeyboardAlwaysEditText.onBackPress(action: suspend () -> Unit) = setOnBackPress(action)