package com.biglabs.mozo.sdk.services

import android.app.Activity

internal class TmpService private constructor(activity: Activity) {

    companion object {
        @Volatile
        private var instance: TmpService? = null

        @Synchronized
        fun initialize(activity: Activity) {
            if (instance == null) {
                instance = TmpService(activity)
            }
        }

        @Synchronized
        fun getInstance(): TmpService {
            if (instance == null) {
                throw IllegalStateException("TmpService is not initialized. Make sure to call TmpService.initialize(Activity) first.")
            }
            return instance as TmpService
        }
    }
}