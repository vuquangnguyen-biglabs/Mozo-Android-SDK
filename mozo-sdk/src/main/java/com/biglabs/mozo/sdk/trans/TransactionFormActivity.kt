package com.biglabs.mozo.sdk.trans

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.services.AddressBookService
import com.biglabs.mozo.sdk.ui.AddressAddActivity
import com.biglabs.mozo.sdk.ui.AddressBookActivity
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.view_transfer.*
import kotlinx.android.synthetic.main.view_transfer_complete.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.web3j.crypto.WalletUtils
import java.util.*


@Suppress("unused")
internal class TransactionFormActivity : AppCompatActivity() {

    private var lastSentAddress: String? = null
    private var lastSentAmount: String? = null
    private var lastSentTime: Long = 0

    private var selectedContact: Models.Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_transfer)

        initUI()
        showInputUI()

        mozo_wallet_balance_rate_side.text = "₩102.230"

        AddressBookService.getInstance().fetchData(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return
        when {
            requestCode == KEY_PICK_ADDRESS -> {
                data?.run {
                    selectedContact = getParcelableExtra(AddressBookActivity.KEY_SELECTED_ADDRESS)
                    showContactInfoUI()
                }
            }
            requestCode == KEY_VERIFY_PIN -> {
                data?.run {
                    sendTx(getStringExtra(SecurityActivity.KEY_DATA))
                }
            }
            data != null -> {
                IntentIntegrator
                        .parseActivityResult(requestCode, resultCode, data)
                        .contents?.let {
                    selectedContact = AddressBookService.getInstance().findByAddress(it)

                    showInputUI()
                    if (selectedContact == null) {
                        output_receiver_address.setText(it)
                        updateSubmitButton()
                    } else
                        showContactInfoUI()
                }
            }
        }
    }

    private fun sendTx(pin: String?) {
        if (pin == null) return
        val address = selectedContact?.soloAddress ?: output_receiver_address.text.toString()
        val amount = output_amount.text.toString()
        launch {
            showLoading()
            val txResponse = MozoTrans.getInstance().createTransaction(address, amount, pin).await()
            lastSentAddress = address
            lastSentAmount = amount
            lastSentTime = Calendar.getInstance().timeInMillis
            showResultUI(txResponse)
            hideLoading()
        }
    }

    override fun onBackPressed() {
        if (output_receiver_address.isEnabled) {
            super.onBackPressed()
        } else {
            showInputUI()
            showContactInfoUI()
        }
    }

    private fun initUI() {
        launch {
            val balance = MozoTrans.getInstance().getBalance().await()
            launch(UI) {
                mozo_wallet_balance_value?.text = balance ?: "0"
            }
        }

        val onTextChanged: (s: CharSequence?) -> Unit = { updateSubmitButton() }
        output_receiver_address.onTextChanged(onTextChanged)
        output_amount.onTextChanged(onTextChanged)

        val decimal = PreferenceUtils.getInstance(this).getDecimal()
        output_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(12, decimal))

        transfer_toolbar.onBackPress = { onBackPressed() }
        button_address_book.click { AddressBookActivity.startForResult(this, KEY_PICK_ADDRESS) }
        button_scan_qr.click {
            IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("")
                    .initiateScan()
        }
        button_submit.click {
            if (output_receiver_address.isEnabled) {
                if (validateInput()) showConfirmationUI()
            } else {
                SecurityActivity.start(this, SecurityActivity.KEY_VERIFY_PIN, KEY_VERIFY_PIN)
            }
        }
    }

    private fun showInputUI() {
        output_receiver_address.isEnabled = true
        output_amount.isEnabled = true
        output_amount_label.setText(R.string.mozo_transfer_amount)
        visible(arrayOf(
                output_receiver_address,
                output_receiver_address_underline,
                button_address_book,
                button_scan_qr,
                output_amount,
                output_amount_underline,
                text_spendable
        ))
        gone(arrayOf(
                output_receiver_address_user,
                output_amount_preview_container
        ))

        transfer_toolbar.setTitle(R.string.mozo_transfer_title)
        transfer_toolbar.showBackButton(false)
        button_submit.setText(R.string.mozo_button_continue)
    }

    private fun showContactInfoUI() {
        updateSubmitButton()
        selectedContact?.run {
            output_receiver_address.visibility = View.INVISIBLE
            output_receiver_address_underline.visibility = View.INVISIBLE

            output_receiver_address_user.visible()
            text_receiver_user_name.text = name
            text_receiver_user_address.text = soloAddress

            button_clear.apply {
                visible()
                click {
                    selectedContact = null
                    showInputUI()
                    updateSubmitButton()
                }
            }
        }
    }

    private fun showConfirmationUI() {
        output_receiver_address.isEnabled = false
        output_amount.isEnabled = false
        output_amount_label.setText(R.string.mozo_transfer_amount_offchain)
        gone(arrayOf(
                output_receiver_address_underline,
                button_address_book,
                button_scan_qr,
                output_amount,
                output_amount_underline,
                text_spendable,
                button_clear
        ))
        text_preview_amount.text = output_amount.text
        output_amount_preview_container.visible()

        transfer_toolbar.setTitle(R.string.mozo_transfer_confirmation)
        transfer_toolbar.showBackButton(true)
        button_submit.setText(R.string.mozo_button_send)
    }

    private fun showResultUI(txResponse: Models.TransactionResponse?) = async(UI) {
        if (txResponse != null) {
            setContentView(R.layout.view_transfer_complete)
            button_close_transfer.click { finishAndRemoveTask() }

            val msg = SpannableString(getString(R.string.mozo_transfer_send_complete_msg, lastSentAmount, lastSentAddress))
            msg.setSpan(
                    StyleSpan(Typeface.BOLD),
                    9,
                    25 + (lastSentAmount ?: "").length,
                    SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            text_send_complete_msg.text = msg

            button_save_address.click {
                AddressAddActivity.start(this@TransactionFormActivity, lastSentAddress)
            }
            button_transaction_detail.click {
                TransactionDetails.start(this@TransactionFormActivity, lastSentAddress, lastSentAmount, lastSentTime)
            }
        } else {
            // TODO show send Tx failed UI
            "send Tx failed UI".logAsError()
        }
    }

    private fun updateSubmitButton() {
        button_submit.isEnabled = (selectedContact != null || output_receiver_address.length() > 0) && output_amount.length() > 0
    }

    private fun showLoading() = async(UI) {
        loading_container.show()
    }

    private fun hideLoading() = async(UI) {
        loading_container.hide()
    }

    @SuppressLint("SetTextI18n")
    private fun validateInput(): Boolean {
        val address = selectedContact?.soloAddress ?: output_receiver_address.text.toString()
        if (!WalletUtils.isValidAddress(address)) {
            AlertDialog.Builder(this).setMessage("The Receiver Address is not valid!").show()
            return false
        }
        if (output_amount.text.startsWith(".")) {
            output_amount.setText("0${output_amount.text}")
        }
        return true
    }

    companion object {
        private const val KEY_PICK_ADDRESS = 0x0021
        private const val KEY_VERIFY_PIN = 0x0022

        fun start(context: Context) {
            Intent(context, TransactionFormActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(this)
            }
        }
    }
}