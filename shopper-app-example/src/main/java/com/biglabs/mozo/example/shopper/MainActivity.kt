package com.biglabs.mozo.example.shopper

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.auth.AuthenticationListener
import com.biglabs.mozo.sdk.services.AuthService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MozoSDK.getInstance().auth.setAuthenticationListener(object : AuthenticationListener() {
            override fun onChanged(isSinged: Boolean) {
                super.onChanged(isSinged)
                updateUI(isSinged)
            }
        })

        viewWalletInfoButton.setOnClickListener {
            startActivity(Intent(this, WalletInfoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val isSigned = AuthService.getInstance().isSignedIn()
        updateUI(isSigned)
    }

    private fun updateUI(isSinged: Boolean) {
        viewWalletInfoButton.visibility = if (isSinged) View.VISIBLE else View.GONE
    }
}
