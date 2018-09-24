package com.biglabs.mozo.sdk.trans

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.utils.displayString
import kotlinx.coroutines.experimental.async

class MozoTrans private constructor() {

    fun getBalance() = async {
        WalletService.getInstance().getAddress().await()?.let {
            val balanceInfo = MozoApiService.getInstance(MozoSDK.context!!).getBalance(it).await()
            return@async balanceInfo.body()?.balanceDisplay().displayString(12)
        }
    }

    companion object {
        @Volatile
        private var instance: MozoTrans? = null

        fun getInstance() = instance ?: synchronized(this) {
            if (instance == null) instance = MozoTrans()
            instance
        }!!
    }
}