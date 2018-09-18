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
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.widget.onBackPress
import com.biglabs.mozo.sdk.utils.*
import kotlinx.android.synthetic.main.view_backup.*
import kotlinx.android.synthetic.main.view_pin_input.*
import kotlinx.android.synthetic.main.view_toolbar.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus


internal class SecurityActivity : AppCompatActivity() {

    private var mPIN = ""
    private var mPINLength = 0
    private var mShowMessageDuration = 0
    private var mRequestCode = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPINLength = getInteger(R.integer.security_pin_length)
        mShowMessageDuration = getInteger(R.integer.security_pin_show_msg_duration)

        mRequestCode = intent.getIntExtra(KEY_REQUEST_CODE, mRequestCode)

        val seed = intent.getStringExtra(KEY_SEED_WORDS)
        when {
            seed != null -> showBackupUI(seed)
            mRequestCode == KEY_ENTER_PIN -> showPinInputRestoreUI()
            else -> {
                finish()
                EventBus.getDefault().post(MessageEvent.Pin(mPIN, mRequestCode))
            }
        }
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

        button_continue.click { showPinInputUI() }
    }

    private fun showPinInputUI() {
        setContentView(R.layout.view_pin_input)

        pin_toolbar.screen_title.setText(R.string.mozo_pin_title)
        sub_title_pin.setText(R.string.mozo_pin_sub_title)
        text_content_pin.setText(R.string.mozo_pin_content)

        input_pin?.apply {
            onBackPress { finish() }

            setMaxLength(mPINLength)
            setOnEditorActionListener { _, actionId, _ ->
                actionId == EditorInfo.IME_ACTION_NEXT && validatePIN()
            }
            onTextChanged {
                hideErrorUI()
                if (it?.length == mPINLength) {
                    when {
                        mRequestCode == KEY_ENTER_PIN -> doResponseResult()
                        !mPIN.isEmpty() && TextUtils.equals(input_pin.text, mPIN) -> doResponseResult()
                        !mPIN.isEmpty() && !TextUtils.equals(input_pin.text, mPIN) -> showErrorUI()
                    }
                }
            }

            launch(UI) {
                delay(500)
                showKeyboard()
            }
        }
        input_pin_checker_status.visibility = View.GONE
    }

    private fun showPinInputRestoreUI() {
        showPinInputUI()
        initRestoreUI()
    }

    private fun initRestoreUI(clearPin: Boolean = false) {
        pin_toolbar.screen_title.setText(R.string.mozo_pin_title_restore)
        sub_title_pin.setText(R.string.mozo_pin_sub_title_restore)

        input_pin?.apply {
            visibility = View.VISIBLE
            if (clearPin) {
                text.clear()
            }
        }

        hideLoadingUI()
    }

    private fun showPinInputConfirmUI() {
        sub_title_pin.setText(R.string.mozo_pin_confirm_sub_title)
        text_content_pin.setText(R.string.mozo_pin_confirm_content)

        input_pin.text.clear()
        input_pin_checker_status.visibility = View.VISIBLE
        input_pin_checker_status.isSelected = false
    }

    private fun showPinCreatedUI() {
        text_correct_pin.setText(R.string.mozo_pin_msg_create_success)
        text_correct_pin.visibility = View.VISIBLE
        input_pin_checker_status.isSelected = true
        input_pin.visibility = View.VISIBLE
        input_pin.isEnabled = false
        hideLoadingUI()
    }

    private fun showPinInputCorrectUI() {
        showPinCreatedUI()
        text_correct_pin.setText(R.string.mozo_pin_msg_enter_correct)
    }

    private fun showErrorUI() {
        text_incorrect_pin.visibility = View.VISIBLE
    }

    private fun hideErrorUI() {
        input_pin_checker_status.isSelected = false
        if (text_incorrect_pin.visibility != View.GONE)
            text_incorrect_pin.visibility = View.GONE
    }

    private fun showLoadingUI() {
        text_correct_pin.visibility = View.GONE
        text_incorrect_pin.visibility = View.GONE
        input_pin.visibility = View.GONE
        input_pin_checker_status.visibility = View.GONE

        input_loading_indicator.visibility = View.VISIBLE
        input_loading_text.visibility = View.VISIBLE
    }

    private fun hideLoadingUI() {
        input_loading_indicator.visibility = View.GONE
        input_loading_text.visibility = View.GONE
    }

    private fun validatePIN(): Boolean {
        when {
            mRequestCode == KEY_CREATE_PIN && mPIN.isEmpty() && input_pin.length() == mPINLength -> {
                mPIN = input_pin.text.toString()
                showPinInputConfirmUI()
            }
        }
        return true
    }

    private fun doResponseResult() {
        launch(UI) {
            showLoadingUI()

            when (mRequestCode) {
                KEY_CREATE_PIN -> {
                    WalletService.getInstance().executeSaveWallet(mPIN).await()
                    showPinCreatedUI()
                }
                KEY_ENTER_PIN -> {
                    val isCorrect = WalletService.getInstance().validatePin(input_pin.text.toString()).await()
                    initRestoreUI(!isCorrect)
                    if (isCorrect) showPinInputCorrectUI()
                    else {
                        showErrorUI()
                        return@launch
                    }
                }
            }
            delay(mShowMessageDuration)

            finish()
            EventBus.getDefault().post(MessageEvent.Pin(mPIN, mRequestCode))
        }
    }

    companion object {
        private const val KEY_SEED_WORDS = "SEED_WORDS"
        private const val KEY_REQUEST_CODE = "REQUEST_CODE"

        const val KEY_CREATE_PIN = 0x001
        const val KEY_ENTER_PIN = 0x002

        fun start(context: Context, seed: String? = null, requestCode: Int) {
            val intent = Intent(context, SecurityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(KEY_SEED_WORDS, seed)
            intent.putExtra(KEY_REQUEST_CODE, requestCode)
            context.startActivity(intent)
        }
    }
}