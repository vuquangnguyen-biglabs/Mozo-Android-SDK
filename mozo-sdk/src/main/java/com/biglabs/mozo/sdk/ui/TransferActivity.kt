package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.trans.MozoTrans
import com.biglabs.mozo.sdk.utils.click
import com.biglabs.mozo.sdk.utils.gone
import com.biglabs.mozo.sdk.utils.onTextChanged
import com.biglabs.mozo.sdk.utils.visible
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.view_transfer.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.web3j.crypto.WalletUtils

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_transfer)

        initUI()
        showInputUI()

        mozo_wallet_balance_rate_side.text = "₩102.230"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return

        when {
            requestCode == KEY_PICK_ADDRESS -> {
                // TODO read picked address
            }
            data != null -> {
                IntentIntegrator
                        .parseActivityResult(requestCode, resultCode, data)
                        .contents?.let {
                    if (WalletUtils.isValidAddress(it)) {
                        input_receiver_address.setText(it)
                    } else {
                        // TODO show warning message
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (input_receiver_address.isEnabled) {
            super.onBackPressed()
        } else showInputUI()
    }

    private fun initUI() {
        launch {
            val balance = MozoTrans.getInstance().getBalance().await()
            launch(UI) {
                mozo_wallet_balance_value?.text = balance
            }
        }

        val onTextChanged: (s: CharSequence?) -> Unit = {
            button_submit.isEnabled = input_receiver_address.length() > 0 && input_amount.length() > 0
        }
        input_receiver_address.onTextChanged(onTextChanged)
        input_amount.onTextChanged(onTextChanged)

        transfer_toolbar.onBackPress = { showInputUI() }
        button_address_book.click { AddressBookActivity.startForResult(this, KEY_PICK_ADDRESS) }
        button_scan_qr.click {
            IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("")
                    .initiateScan()
        }
        button_submit.click {
            if (input_receiver_address.isEnabled) {
                showConfirmationUI()
            } else {
                val input = input_receiver_address.text.toString()
                val amount = input_amount.text.toString()
                MozoTrans.getInstance().createTransaction(input, amount)
            }
        }
    }

    private fun showInputUI() {
        input_receiver_address.isEnabled = true
        input_amount.isEnabled = true
        input_amount_label.setText(R.string.mozo_transfer_amount)
        visible(arrayOf(
                input_receiver_address_underline,
                button_address_book,
                button_scan_qr,
                input_amount,
                input_amount_underline,
                text_spendable
        ))
        input_amount_preview_container.gone()

        transfer_toolbar.setTitle(R.string.mozo_transfer_title)
        transfer_toolbar.showBackButton(false)
        button_submit.setText(R.string.mozo_button_continue)
    }

    private fun showConfirmationUI() {
        input_receiver_address.isEnabled = false
        input_amount.isEnabled = false
        input_amount_label.setText(R.string.mozo_transfer_amount_offchain)
        gone(arrayOf(
                input_receiver_address_underline,
                button_address_book,
                button_scan_qr,
                input_amount,
                input_amount_underline,
                text_spendable
        ))
        text_preview_amount.text = input_amount.text
        input_amount_preview_container.visible()

        transfer_toolbar.setTitle(R.string.mozo_transfer_confirmation)
        transfer_toolbar.showBackButton(true)
        button_submit.setText(R.string.mozo_button_send)
    }

    companion object {
        private const val KEY_PICK_ADDRESS = 0x0021

        fun start(context: Context) {
            val starter = Intent(context, TransferActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(starter)
        }
    }
}