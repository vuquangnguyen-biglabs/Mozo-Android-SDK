package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.trans.MozoTrans
import com.biglabs.mozo.sdk.utils.click
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.view_transfer.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class TransferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_transfer)

        launch {
            val balance = MozoTrans.getInstance().getBalance().await()
            launch(UI) {
                mozo_wallet_balance_value?.text = balance
            }
        }

        button_address_book.click { AddressBookActivity.startForResult(this, KEY_PICK_ADDRESS) }
        button_scan_qr.click {
            IntentIntegrator(this)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("")
                    .initiateScan()
        }

        input_receiver_address.setText("0x327b993ce7201a6e8b1df02256910c9ea6bc4865")
        input_amount.setText("1")
        mozo_wallet_balance_rate_side.text = "₩102.230"

        button_continue.isEnabled = true
        button_continue.click {
            val input = input_receiver_address.text.toString()
            val amount = input_amount.text.toString()
            MozoTrans.getInstance().createTransaction(input, amount)
        }
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
                    input_receiver_address.setText(it)
                }
            }
        }
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