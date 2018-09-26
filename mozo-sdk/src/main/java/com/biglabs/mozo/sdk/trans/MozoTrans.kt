package com.biglabs.mozo.sdk.trans

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.ui.TransferActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import com.biglabs.mozo.sdk.utils.displayString
import com.biglabs.mozo.sdk.utils.logAsError
import com.google.gson.Gson
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.utils.Numeric

class MozoTrans private constructor() {

    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }
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
        /*
        if (!EventBus.getDefault().isRegistered(this@MozoTrans)) {
            EventBus.getDefault().register(this@MozoTrans)
        }
        SecurityActivity.start(MozoSDK.context!!, requestCode = SecurityActivity.KEY_ENTER_PIN)
*/
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
                if (response.isSuccessful && response.body() != null) {
                    response.body().toString().logAsError()

                    val toSign = response.body()!!.toSign[0]
                    val private = "B663C243251CDECC0BAA7056A6A1BB9ECB499C5503EDC2CC79A3553C49C5C5B1"

                    val credentials = Credentials.create(private)
                    val signatureData = Sign.signMessage(Numeric.hexStringToByteArray(toSign), credentials.ecKeyPair, false)

                    val signature = CryptoUtils.serializeSignature(signatureData)
                    signature.logAsError("final")

                    val pubKey = Numeric.toHexStringWithPrefixSafe(credentials.ecKeyPair.publicKey)
                    pubKey.logAsError("publicKey")

                } else {
                    response.message().toString().logAsError()
                }
            }
        }
    }

    @Subscribe
    fun onReceivePin(event: MessageEvent.Pin) {
        EventBus.getDefault().unregister(this@MozoTrans)

        launch {

            mozoDB.profile().getCurrentUserProfile()?.run {
                val privateKey = CryptoUtils.decrypt(walletInfo?.privateKey!!, event.pin)
                privateKey?.logAsError("privateKey")


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