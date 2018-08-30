package com.biglabs.mozo.sdk.services

import android.Manifest
import android.content.Context
import android.os.Environment
import android.util.Log
import com.biglabs.mozo.sdk.utils.PermissionUtils
import org.web3j.crypto.WalletUtils

internal class WalletService private constructor(context: Context) {
    fun createWallet(context: Context) {
        PermissionUtils.requestPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            val wallet = WalletUtils.generateBip39Wallet("0000", Environment.getExternalStorageDirectory())
            Log.e("vu", "Wallet service init: \n wallet: " + wallet + ", dir: " + Environment.getExternalStorageDirectory())
        }
    }

    companion object {
        @Volatile
        private var instance: WalletService? = null

        @Synchronized
        internal fun getInstance(context: Context): WalletService {
            if (instance == null) {
                instance = WalletService(context)
            }
            return instance as WalletService
        }
    }
}