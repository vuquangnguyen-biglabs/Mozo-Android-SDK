package com.biglabs.mozo.sdk.trans

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.TransferActivity
import com.biglabs.mozo.sdk.utils.displayString
import com.biglabs.mozo.sdk.utils.logAsError
import com.google.gson.Gson
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus

class MozoTrans private constructor() {

    private var mCurrentAddress: String? = null

    init {
        launch {
            mCurrentAddress = WalletService.getInstance().getAddress().await()
        }
    }

    fun getBalance() = async {
        val balanceInfo = MozoApiService
                .getInstance(MozoSDK.context!!)
                .getBalance(mCurrentAddress!!)
                .await()
        return@async balanceInfo.body()?.balanceDisplay().displayString(12)
    }

    fun transfer() {
        MozoSDK.context?.run {
            //            if (!EventBus.getDefault().isRegistered(this@MozoTrans)) {
//                EventBus.getDefault().register(this@MozoTrans)
//            }
            TransferActivity.start(this)
            return
        }
    }

    internal fun createTransaction(input: String, amount: String) {
        mCurrentAddress?.let {
            val inAdds = arrayListOf(Models.TransactionAddress(arrayListOf(input)))
            val outAdds = arrayListOf(Models.TransactionAddressOutput(arrayListOf(input), amount.toBigDecimal()))
            launch {
                val response = MozoApiService
                        .getInstance(MozoSDK.context!!)
                        .createTransaction(
                                Models.TransactionRequest(inAdds, outAdds)
                        )
                        .await()
                if (response.isSuccessful) {
                    response.body().toString().logAsError()
                    
                } else {
                    response.message().toString().logAsError()
                }
            }

        }
    }

    internal fun sendTransaction() {

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