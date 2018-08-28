package com.biglabs.mozo.sdk

import android.content.Context
import com.biglabs.mozo.sdk.services.BeaconService

class MozoSDK private constructor(context: Context) {

    init {
        BeaconService.initialize(context)
    }

    fun stopScan() {
        BeaconService.getInstance().stopScan()
    }

    companion object {
        @Volatile
        private var instance: MozoSDK? = null

        @JvmStatic
        @Synchronized
        fun initialize(applicationContext: Context) {
            if (instance == null) {
                instance = MozoSDK(applicationContext)
            }
        }

        @JvmStatic
        @Synchronized
        fun getInstance(): MozoSDK {
            if (instance == null) {
                throw IllegalStateException("MozoSDK is not initialized. Make sure to call MozoSDK.initialize(Context) first.")
            }
            return instance as MozoSDK
        }
    }
}