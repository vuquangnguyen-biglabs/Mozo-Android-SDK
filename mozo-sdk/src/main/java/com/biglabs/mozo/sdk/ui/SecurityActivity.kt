package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.ui.views.onBackPress
import com.biglabs.mozo.sdk.utils.hideSotfKeyboard
import kotlinx.android.synthetic.main.view_backup.*
import kotlinx.android.synthetic.main.view_pin_input.*
import org.greenrobot.eventbus.EventBus

class SecurityActivity : AppCompatActivity() {

    private var pin = ""
    private var seed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_pin_input)

        seed = intent.getStringExtra(KEY_SEED_WORDS)

        input_pin.onBackPress {
            finish()
        }

        input_pin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT && validatePIN()) {
                when {
                    pin.isEmpty() -> {
                        pin = input_pin.text.toString()
                        if (seed != null) showConfirmUI()
                        else doResponseResult()
                    }
                    TextUtils.equals(input_pin.text, pin) -> showBackupUI()
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

    private fun showBackupUI() {
        hideSotfKeyboard(input_pin)
        setContentView(R.layout.view_backup)
        seed_view.text = seed
        done_button.setOnClickListener { doResponseResult() }
    }

    private fun doResponseResult() {
        finish()
        EventBus.getDefault().post(MessageEvent.Pin(pin))
    }

    companion object {
        private const val KEY_SEED_WORDS = "SEED_WORDS"

        fun start(context: Context, seed: String? = null) {
            val intent = Intent(context, SecurityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(KEY_SEED_WORDS, seed)
            context.startActivity(intent)
        }
    }
}