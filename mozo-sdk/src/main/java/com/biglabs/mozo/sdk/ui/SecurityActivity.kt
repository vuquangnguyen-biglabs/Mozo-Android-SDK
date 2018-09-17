package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.ui.views.onBackPress
import com.biglabs.mozo.sdk.utils.dp2Px
import com.biglabs.mozo.sdk.utils.hideSoftKeyboard
import com.biglabs.mozo.sdk.utils.showSoftKeyboard
import kotlinx.android.synthetic.main.view_backup.*
import kotlinx.android.synthetic.main.view_pin_input.*
import org.greenrobot.eventbus.EventBus


internal class SecurityActivity : AppCompatActivity() {

    private var pin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seed = intent.getStringExtra(KEY_SEED_WORDS)
        if (seed != null)
            showBackupUI(seed)
        else
            showPinInputUI()
    }

    override fun onStop() {
        super.onStop()
        if (input_pin != null) {
            hideSoftKeyboard(input_pin)
        }
    }

    private fun showBackupUI(seed: String) {
        setContentView(R.layout.view_backup)

        val padding = resources.dp2Px(10f).toInt()
        seed.split(" ").map {
            val word = TextView(this@SecurityActivity)
            word.setPaddingRelative(padding, padding, padding, padding)
            word.text = it
            TextViewCompat.setTextAppearance(word, R.style.MozoTheme_SeedWords)
            seed_view.addView(word)
        }

        button_stored_confirm.setOnCheckedChangeListener { _, isChecked ->
            button_continue.isEnabled = isChecked
        }

        button_continue.setOnClickListener { showPinInputUI() }
    }

    private fun showPinInputUI() {
        setContentView(R.layout.view_pin_input)
        input_pin.requestFocus()
        showSoftKeyboard(input_pin)
        input_pin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT && validatePIN()) {
                when {
                    pin.isEmpty() -> {
                        pin = input_pin.text.toString()
                        showConfirmUI()
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

        input_pin.onBackPress { finish() }
    }

    private fun showConfirmUI() {
        //screen_title.text = "Confirm PIN"
        input_pin.text = null
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