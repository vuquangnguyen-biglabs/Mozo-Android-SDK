package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class AddressAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun start(context: Context, address: String?) {
            val starter = Intent(context, AddressAddActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(starter)
        }
    }
}