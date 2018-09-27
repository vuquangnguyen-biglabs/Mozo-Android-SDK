package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TransactionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    companion object {
        fun start(context: Context, address: String?, amount: String?, sentTime: Long) {
            val starter = Intent(context, TransactionDetails::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(starter)
        }
    }
}