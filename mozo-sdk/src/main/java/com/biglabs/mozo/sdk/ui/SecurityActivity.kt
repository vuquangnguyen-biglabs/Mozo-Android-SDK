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
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.views.onBackPress
import kotlinx.android.synthetic.main.activity_security.*

class SecurityActivity : AppCompatActivity() {

    private var pin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        val isShowConfirm = intent.getBooleanExtra(KEY_SHOW_CONFIRM, false)

        input_pin.onBackPress {
            finish()
        }

        input_pin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT && validatePIN()) {
                when {
                    pin.isEmpty() -> {
                        pin = input_pin.text.toString()
                        if (isShowConfirm) showConfirmUI()
                        else doResponseResult()
                    }
                    TextUtils.equals(input_pin.text, pin) -> doResponseResult()
                    else -> AlertDialog.Builder(this)
                            .setMessage("PIN does not match")
                            .setNegativeButton(android.R.string.ok, null)
                            .create().show()
                }
                true
            } else false
        }
    }

    private fun validatePIN(): Boolean {
        val isValid = input_pin.length() == 4
        if (!isValid) {
            AlertDialog.Builder(this)
                    .setMessage("PIN is not enough")
                    .setNegativeButton(android.R.string.ok, null)
                    .create().show()
        }
        return isValid
    }

    private fun showConfirmUI() {
        title = "Confirm PIN"
        screen_title.text = "Confirm PIN"
        input_pin.text = null
    }

    private fun doResponseResult() {
        finish()
        WalletService.getInstance().onReceivePin(pin)
    }

    companion object {
        private const val KEY_SHOW_CONFIRM = "SHOW_CONFIRM"

        fun start(context: Context) = start(context, false)
        fun start(context: Context, showConfirm: Boolean) {
            val intent = Intent(context, SecurityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(KEY_SHOW_CONFIRM, showConfirm)
            context.startActivity(intent)
        }
    }
}