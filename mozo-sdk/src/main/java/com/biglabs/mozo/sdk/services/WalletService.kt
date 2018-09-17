package com.biglabs.mozo.sdk.services

import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.core.Models.Profile
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom


internal class WalletService private constructor() {

    var userId: String? = null
    var seed: String? = null
    var privKey: String? = null
    var address: String? = null

    fun initWallet() {
        MozoSDK.context?.let {
            launch {
                val profile = MozoDatabase.getInstance(it).profile().getCurrentUserProfile()
                profile?.run {
                    if (walletInfo == null) {
                        /* Server wallet is NOT existing, create a new one at local */
                        executeCreateWallet()
                        /* Required input new PIN */
                        /* After input PIN will be continue at onReceivePin() fun */
                    } else if (walletInfo.privateKey == null) {
                        /* Local wallet is existing but no private Key */
                        /* Required input previous PIN */
                        // TODO show PIN UI
                        /* After input PIN will be continue at onReceivePin() fun */
                    }
                }
            }
        }
    }

    private fun executeCreateWallet() {
        MozoSDK.context?.let {
            val mnemonic = MnemonicUtils.generateMnemonic(
                    SecureRandom().generateSeed(16)
            )

            val key = DeterministicKeyChain
                    .builder()
                    .seed(DeterministicSeed(mnemonic, null, "", System.nanoTime()))
                    .build()
                    .getKeyByPath(HDUtils.parsePath(ETH_FIRST_ADDRESS_PATH), true)
            this@WalletService.privKey = key.privKey.toString(16)

            // Web3j
            val credentials = Credentials.create(privKey)
            this@WalletService.seed = mnemonic
            this@WalletService.address = credentials.address
            if (!EventBus.getDefault().isRegistered(this@WalletService)) {
                EventBus.getDefault().register(this@WalletService)
            }
            SecurityActivity.start(it, mnemonic)
        }
    }

    @Subscribe
    fun onReceivePin(event: MessageEvent.Pin) {
        EventBus.getDefault().unregister(this@WalletService)
        if (userId == null) return
        launch {
            val pin = event.pin
            val profile = Profile(
                    userId = userId!!,
                    walletInfo = Models.WalletInfo(
                            CryptoUtils.encrypt(this@WalletService.seed!!, pin),
                            this@WalletService.address!!,
                            CryptoUtils.encrypt(this@WalletService.privKey!!, pin)
                    )
            )
            MozoDatabase.getInstance(MozoSDK.context!!).profile().save(profile)

            // TODO Send the wallet to server

            launch(UI) {
                Toast.makeText(MozoSDK.context, "Your new wallet has been created", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val ETH_FIRST_ADDRESS_PATH = "M/44H/60H/0H/0/0"

        @Volatile
        private var INSTANCE: WalletService? = null

        fun getInstance() = INSTANCE ?: synchronized(this) {
            if (INSTANCE == null) INSTANCE = WalletService()
            INSTANCE
        }!!
    }
}