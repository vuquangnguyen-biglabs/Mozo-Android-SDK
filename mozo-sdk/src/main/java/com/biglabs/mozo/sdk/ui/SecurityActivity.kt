package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.ui.views.onBackPress
import kotlinx.android.synthetic.main.activity_security.*

class SecurityActivity : AppCompatActivity() {

    private var pin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        input_pin.onBackPress {
            finish()
        }

        input_pin.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                when {
                    pin.isEmpty() -> {
                        pin = input_pin.text.toString()
                        showConfirmUI()
                    }
                    TextUtils.equals(input_pin.text, pin) -> doResponseResult()
                    else -> AlertDialog.Builder(this)
                            .setMessage("")
                            .create().show()
                }
                true
            } else false
        }
    }

    private fun showConfirmUI() {
        title = "Confirm PIN"
        input_pin.text = null
    }

    private fun doResponseResult() {

    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SecurityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}