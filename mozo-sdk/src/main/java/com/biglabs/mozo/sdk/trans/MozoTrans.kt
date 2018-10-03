package com.biglabs.mozo.sdk.trans

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.TransactionHistoryActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import com.biglabs.mozo.sdk.utils.PreferenceUtils
import com.biglabs.mozo.sdk.utils.displayString
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.async
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigDecimal

class MozoTrans private constructor() {

    private val mPreferenceUtils: PreferenceUtils by lazy { PreferenceUtils.getInstance(MozoSDK.context!!) }

    private val decimalRate: Double

    init {
        val decimal = mPreferenceUtils.getDecimal()
        decimalRate = Math.pow(10.0, decimal.toDouble())
    }

    fun getBalance() = async {
        val address = WalletService.getInstance().getAddress().await() ?: return@async null
        val balanceInfo = MozoApiService
                .getInstance(MozoSDK.context!!)
                .getBalance(address)
                .await()
        mPreferenceUtils.setDecimal(balanceInfo.body()?.decimals ?: -1)
        return@async balanceInfo.body()?.balanceDisplay()
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

    fun openTransactionHistory() {
        TransactionHistoryActivity.start(MozoSDK.context!!)
    }

    internal fun createTransaction(output: String, amount: String, pin: String) = async {
        val myAddress = WalletService.getInstance().getAddress().await() ?: return@async null
        val response = MozoApiService
                .getInstance(MozoSDK.context!!)
                .createTransaction(
                        prepareRequest(myAddress, output, amount)
                )
                .await()
        if (response.isSuccessful && response.body() != null) {
            val txResponse = response.body()!!

            val privateKeyEncrypted = WalletService.getInstance().getPrivateKeyEncrypted().await()
            val privateKey = CryptoUtils.decrypt(privateKeyEncrypted, pin)
            privateKey?.logAsError("raw privateKey")

            val toSign = txResponse.toSign[0]
            val credentials = Credentials.create(privateKey)
            val signatureData = Sign.signMessage(Numeric.hexStringToByteArray(toSign), credentials.ecKeyPair, false)

            val signature = CryptoUtils.serializeSignature(signatureData)
            signature.logAsError("signature")
            val pubKey = Numeric.toHexStringWithPrefixSafe(credentials.ecKeyPair.publicKey)
            pubKey.logAsError("pubKey")
            txResponse.signatures = arrayListOf(signature)
            txResponse.publicKeys = arrayListOf(pubKey)

            return@async sendTransaction(txResponse).await()
        } else {
            response.message().toString().logAsError("create TX")
            return@async null
        }
    }

    private fun sendTransaction(request: Models.TransactionResponse) = async {
        val txSentResponse = MozoApiService
                .getInstance(MozoSDK.context!!)
                .sendTransaction(request)
                .await()

        if (txSentResponse.isSuccessful && txSentResponse.body() != null) {
            return@async txSentResponse.body()
        } else {
            txSentResponse.message().toString().logAsError("send TX")
            return@async null
        }
    }

    private fun prepareRequest(inAdd: String, outAdd: String, amount: String): Models.TransactionRequest {
        val finalAmount = amount.toBigDecimal().multiply(BigDecimal.valueOf(decimalRate))
        return Models.TransactionRequest(
                arrayListOf(Models.TransactionAddress(arrayListOf(inAdd))),
                arrayListOf(Models.TransactionAddressOutput(arrayListOf(outAdd), finalAmount))
        )
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