package com.biglabs.mozo.sdk.services

import android.content.Context

internal class TmpService private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: TmpService? = null

        @Synchronized
        fun getInstance(context: Context): TmpService {
            if (instance == null) {
                instance = TmpService(context)
            }
            return instance as TmpService
        }
    }
}