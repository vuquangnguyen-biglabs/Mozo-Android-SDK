package com.biglabs.mozo.example.encrypttool

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.biglabs.mozo.sdk.utils.CryptoUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_encrypt.setOnClickListener {
            if (validate()) {
                loading_container.visibility = View.VISIBLE

                val pin = input_pin.text.toString()
                val data = input_data.text.toString()

                launch {
                    val result = CryptoUtils.encrypt(data, pin)
                    launch(UI) {
                        text_result.text = result
                        loading_container.visibility = View.GONE
                    }
                }
            }
        }
        button_decrypt.setOnClickListener {
            if (validate()) {
                loading_container.visibility = View.VISIBLE

                val pin = input_pin.text.toString()
                val data = input_data.text.toString()

                launch {
                    val result = CryptoUtils.decrypt(data, pin)
                    launch(UI) {
                        text_result.text = result
                        loading_container.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        return input_pin.length() > 0 && input_data.length() > 0
    }
}
