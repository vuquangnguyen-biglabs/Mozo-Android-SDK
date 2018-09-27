package com.biglabs.mozo.sdk.services

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import com.biglabs.mozo.sdk.utils.PreferenceUtils
import com.biglabs.mozo.sdk.utils.logAsError
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom

@Suppress("unused")
internal class WalletService private constructor() {

    private val mozoDB: MozoDatabase by lazy { MozoDatabase.getInstance(MozoSDK.context!!) }

    private var seed: String? = null
    private var address: String? = null
    private var privateKey: String? = null
    private var privateKeyEncrypted: String? = null

    init {
        getAddress()
    }

    fun initWallet() {
        MozoSDK.context?.let {
            launch {
                val profile = mozoDB.profile().getCurrentUserProfile()
                profile?.run {
                    if (walletInfo == null || walletInfo!!.encryptSeedPhrase.isNullOrEmpty()) {
                        /* Server wallet is NOT existing, create a new one at local */
                        executeCreateWallet()
                        /* Required input new PIN */
                        /* After input PIN will be continue at onReceivePin() fun */
                    } else {
                        if (walletInfo!!.privateKey == null) {
                            /* Local wallet is existing but no private Key */
                            /* Required input previous PIN */
                            if (!EventBus.getDefault().isRegistered(this@WalletService)) {
                                EventBus.getDefault().register(this@WalletService)
                            }
                            SecurityActivity.start(it, requestCode = SecurityActivity.KEY_ENTER_PIN)
                            /* After input PIN will be continue at onReceivePin() fun */
                        }
                        if (PreferenceUtils.getInstance(it).getFlag(PreferenceUtils.FLAG_SYNC_WALLET_INFO)) {
                            syncWalletInfo(walletInfo!!)
                        }
                    }
                }
            }
        }
    }

    private fun clearVariables() {
        this@WalletService.seed = null
        this@WalletService.privateKey = null
    }

    private fun executeCreateWallet() {
        clearVariables()
        MozoSDK.context?.let {
            val mnemonic = MnemonicUtils.generateMnemonic(
                    SecureRandom().generateSeed(16)
            )
            this@WalletService.seed = mnemonic
            this@WalletService.privateKey = CryptoUtils.getFirstAddressPrivateKey(mnemonic)

            val credentials = Credentials.create(privateKey)
            this@WalletService.address = credentials.address
            if (!EventBus.getDefault().isRegistered(this@WalletService)) {
                EventBus.getDefault().register(this@WalletService)
            }
            SecurityActivity.start(it, mnemonic, SecurityActivity.KEY_CREATE_PIN)
        }
    }

    fun executeSaveWallet(pin: String) = async {
        val profile = mozoDB.profile().getCurrentUserProfile()
        profile?.let {
            it.walletInfo = Models.WalletInfo(
                    CryptoUtils.encrypt(this@WalletService.seed!!, pin),
                    this@WalletService.address!!,
                    CryptoUtils.encrypt(this@WalletService.privateKey!!, pin)
            )

            /* save wallet info to local */
            mozoDB.profile().save(it)

            /* save wallet info to server */
            syncWalletInfo(it.walletInfo!!).await()
        }
        clearVariables()
    }

    private fun syncWalletInfo(walletInfo: Models.WalletInfo) = async {
        MozoSDK.context?.let {
            val response = MozoApiService.getInstance(MozoSDK.context!!).saveWallet(walletInfo).await()
            PreferenceUtils.getInstance(it).setFlag(
                    PreferenceUtils.FLAG_SYNC_WALLET_INFO,
                    !response.isSuccessful
            )
        }
    }

    fun validatePin(pin: String) = async {
        val profile = mozoDB.profile().getCurrentUserProfile()
        profile?.walletInfo?.run {
            if (encryptSeedPhrase.isNullOrEmpty() || pin.isEmpty()) return@async false
            else return@async try {
                var decrypted = CryptoUtils.decrypt(encryptSeedPhrase!!, pin)
                val isCorrect = !decrypted.isNullOrEmpty() && MnemonicUtils.validateMnemonic(decrypted)
                if (isCorrect) {
                    privateKey = CryptoUtils.encrypt(
                            CryptoUtils.getFirstAddressPrivateKey(decrypted!!),
                            pin
                    )
                    mozoDB.profile().save(profile)
                }
                decrypted?.logAsError("mnemonic")
                @Suppress("UNUSED_VALUE")
                decrypted = null
                isCorrect
            } catch (ex: Exception) {
                false
            }
        }

        return@async false
    }

    @Subscribe
    fun onReceivePin(event: MessageEvent.Pin) {
        EventBus.getDefault().unregister(this@WalletService)
        /* load data to variables */
        getAddress()
        address?.logAsError("address after synchronize")
    }

    fun getAddress() = async {
        if (address == null) {
            address = mozoDB.profile().getCurrentUserProfile()?.walletInfo?.offchainAddress
            privateKeyEncrypted = mozoDB.profile().getCurrentUserProfile()?.walletInfo?.privateKey
        }
        return@async address
    }

    internal fun getPrivateKeyEncrypted() = async {
        if (privateKeyEncrypted == null) {
            privateKeyEncrypted = mozoDB.profile().getCurrentUserProfile()?.walletInfo?.privateKey
        }
        return@async privateKeyEncrypted!!
    }

    companion object {
        @Volatile
        private var instance: WalletService? = null

        fun getInstance() = instance ?: synchronized(this) {
            if (instance == null) instance = WalletService()
            instance
        }!!
    }
}