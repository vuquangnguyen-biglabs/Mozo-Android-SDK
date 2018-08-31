package com.biglabs.mozo.example.shopper

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biglabs.mozo.sdk.MozoSDK
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signInBtn.setOnClickListener {
            MozoSDK.getInstance().auth.signIn()
        }
    }

    override fun onStop() {
        super.onStop()
        //MozoSDK.getInstance().beacon.stopScan()
    }
}
