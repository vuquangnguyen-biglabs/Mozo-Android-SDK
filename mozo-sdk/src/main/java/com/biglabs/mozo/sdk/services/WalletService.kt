package com.biglabs.mozo.sdk.services

import android.content.Context
import android.os.Environment
import android.util.Log
import org.web3j.crypto.WalletUtils

internal class WalletService private constructor(context: Context) {

    init {
//        val wallet = WalletUtils.generateFullNewWalletFile("0000", Environment.getExternalStorageDirectory())
//        Log.e("vu", "Wallet service init: \n wallet: " + wallet + ", dir: " + Environment.getExternalStorageDirectory())

        WalletUtils.generateFullNewWalletFile("0000", Environment.getExternalStorageDirectory())
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