package com.biglabs.mozo.sdk.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.PermissionUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.web3j.crypto.WalletUtils

internal class WalletService private constructor() {

    fun createWallet() {
        MozoSDK.context?.let {
            if (PermissionUtils.requestPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                executeCreateWallet()
            }
        }
    }

    fun onPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {
        grantResults.mapIndexed { index, value ->
            if (value == PackageManager.PERMISSION_GRANTED) {
                when (permissions[index]) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        executeCreateWallet()
                    }
                }
            }
        }
    }

    private fun executeCreateWallet() {
        MozoSDK.context?.let {
            SecurityActivity.start(it)
            launch {
                val wallet = WalletUtils.generateBip39Wallet("0000", Environment.getExternalStorageDirectory())

                val msg = "Wallet service init: \n wallet: " + wallet + ", dir: " + Environment.getExternalStorageDirectory()
                Log.e("vu", msg)
                launch(UI) {
                    Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WalletService? = null

        fun getInstance(): WalletService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: WalletService()
                }
    }
}