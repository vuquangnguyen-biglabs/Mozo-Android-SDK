package com.biglabs.mozo.example.shopper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.biglabs.mozo.sdk.ui.view.WalletInfoView
import kotlinx.android.synthetic.main.activity_wallet_info.*

class WalletInfoActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_info)

        initializeViews()
    }

    private fun initializeViews() {
        spinner_view_mode.onItemSelectedListener = this
        val categories = ArrayList<String>()
        categories.add("Address and balance")
        categories.add("Only Address")
        categories.add("Only Balance")

        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner_view_mode.adapter = dataAdapter

        checkbox_show_qr.setOnCheckedChangeListener { _, isChecked ->
            wallet_info_view.setShowQRCode(isChecked)
        }

        checkbox_show_copy.setOnCheckedChangeListener { _, isChecked ->
            wallet_info_view.setShowCopy(isChecked)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> wallet_info_view.setViewMode(WalletInfoView.MODE_ADDRESS_BALANCE)
            1 -> wallet_info_view.setViewMode(WalletInfoView.MODE_ONLY_ADDRESS)
            2 -> wallet_info_view.setViewMode(WalletInfoView.MODE_ONLY_BALANCE)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
