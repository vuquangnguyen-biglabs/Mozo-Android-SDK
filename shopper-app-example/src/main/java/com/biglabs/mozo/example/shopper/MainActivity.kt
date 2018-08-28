package com.biglabs.mozo.example.shopper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.biglabs.mozo.sdk.MozoSDK

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MozoSDK.initialize(this)
        MozoSDK.getInstance()
    }

    override fun onStop() {
        super.onStop()
        //MozoSDK.getInstance().stopScan()
    }
}
