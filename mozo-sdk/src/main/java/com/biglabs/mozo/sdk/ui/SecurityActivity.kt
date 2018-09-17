package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.ui.views.onBackPress
import com.biglabs.mozo.sdk.utils.dp2Px
import com.biglabs.mozo.sdk.utils.getInteger
import com.biglabs.mozo.sdk.utils.hideKeyboard
import com.biglabs.mozo.sdk.utils.showKeyboard
import kotlinx.android.synthetic.main.view_backup.*
import kotlinx.android.synthetic.main.view_pin_input.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus


internal class SecurityActivity : AppCompatActivity() {

    private var pin = ""
    private var pinLength = 0
    private var showMessageDuration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pinLength = getInteger(R.integer.security_pin_length)
        showMessageDuration = getInteger(R.integer.security_pin_show_msg_duration)

        val seed = intent.getStringExtra(KEY_SEED_WORDS)
        if (seed != null)
            showBackupUI(seed)
        else
            showPinInputUI()
    }

    override fun onStop() {
        super.onStop()
        input_pin?.hideKeyboard()
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

        sub_title_pin.setText(R.string.mozo_pin_sub_title)
        text_content_pin.setText(R.string.mozo_pin_content)

        input_pin.setMaxLength(pinLength)
        input_pin.setOnEditorActionListener { _, actionId, _ ->
            actionId == EditorInfo.IME_ACTION_NEXT && validatePIN()
        }
        
        input_pin.onBackPress { finish() }

        input_pin.requestFocus()
        input_pin.showKeyboard()
        input_pin_checker_status.visibility = View.GONE
    }

    private fun showConfirmUI() {
        sub_title_pin.setText(R.string.mozo_pin_confirm_sub_title)
        text_content_pin.setText(R.string.mozo_pin_confirm_content)

        input_pin.text = null
        input_pin_checker_status.visibility = View.VISIBLE
        input_pin_checker_status.isSelected = false
    }

    private fun showErrorUI() {
        text_incorrect_pin.visibility = View.VISIBLE
    }

    private fun validatePIN(): Boolean {
        when {
            pin.isEmpty() && input_pin.length() == pinLength -> {
                pin = input_pin.text.toString()
                showConfirmUI()
            }
            !pin.isEmpty() && TextUtils.equals(input_pin.text, pin) -> doResponseResult()
            !pin.isEmpty() && !TextUtils.equals(input_pin.text, pin) -> showErrorUI()

        }
        return true
    }

    private fun doResponseResult() {
        input_pin_checker_status.isSelected = true
        launch(UI) {
            delay(showMessageDuration)
            finish()
            EventBus.getDefault().post(MessageEvent.Pin(pin))
        }
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