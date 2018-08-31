package com.biglabs.mozo.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

class KeyboardAlwaysEditText : EditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, def: Int) : super(context, attributes, def)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return false
    }
}