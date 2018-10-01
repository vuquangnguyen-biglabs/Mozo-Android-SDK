package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.utils.*
import kotlinx.android.synthetic.main.view_address_add_new.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class AddressAddActivity : AppCompatActivity() {

    private val mozoService by lazy { MozoApiService.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_address_add_new)

        val address = intent.getStringExtra(FLAG_ADDRESS)
        text_contact_address.text = address

        input_contact_name.onTextChanged {
            button_save.isEnabled = input_contact_name.length() > 0
        }

        button_save.click {
            showLoading()

            val contact = Models.Contact(0, input_contact_name.text.toString().trim(), address)
            launch {
                val response = mozoService.saveContact(contact).await()
                if (response.isSuccessful) {
                    showDoneMsg()
                } else {
                    hideLoading()
                    showErrorMsg()
                }
            }
        }
    }

    private fun showDoneMsg() = async(UI) {
        input_contact_name.isEnabled = false
        loading_container.hide()
        text_msg_saved.visible()
    }

    private fun showErrorMsg() = async(UI) {
        text_msg_error.visible()
    }

    private fun showLoading() {
        input_contact_name.hideKeyboard()
        button_save.isEnabled = false
        loading_container.show()
        text_msg_saved.gone()
        text_msg_error.gone()
    }

    private fun hideLoading() = async(UI) {
        input_contact_name.requestFocus()
        input_contact_name.showKeyboard()
        button_save.isEnabled = true
        loading_container.hide()
        text_msg_saved.gone()
    }

    companion object {
        private const val FLAG_ADDRESS = "FLAG_ADDRESS"

        fun start(context: Context, address: String?) {
            Intent(context, AddressAddActivity::class.java).apply {
                putExtra(FLAG_ADDRESS, address)
                context.startActivity(this)
            }
        }
    }
}