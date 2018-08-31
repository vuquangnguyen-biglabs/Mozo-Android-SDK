package com.biglabs.mozo.sdk.services

internal class TmpService private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: TmpService? = null

        @Synchronized
        fun getInstance(): TmpService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: TmpService()
                }
    }
}