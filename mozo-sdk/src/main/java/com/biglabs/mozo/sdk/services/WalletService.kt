package com.biglabs.mozo.sdk.services

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.core.MozoDatabase
import com.biglabs.mozo.sdk.core.Models.Profile
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import com.biglabs.mozo.sdk.utils.PermissionUtils
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

    fun initWallet(userId: String) {
        this.userId = userId
        MozoSDK.context?.let {

            launch {
                if (MozoDatabase.getInstance(it).profile().get(userId) == null) {
                    // TODO check server wallet is existing ?
                    // if true
                    // TODO recover wallet
                    // else
                    executeCreateWallet()
                }
            }
        }
    }

    fun onPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {
        grantResults.mapIndexed { index, value ->
            if (value == PackageManager.PERMISSION_GRANTED) {
                when (permissions[index]) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        executeCreateWallet()
                    }
                }
            }
        }
    }

    private fun executeCreateWallet() {
        MozoSDK.context?.let {
            if (PermissionUtils.requestPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                EventBus.getDefault().register(this@WalletService)
                SecurityActivity.start(it, mnemonic)
            }
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
                    seed = CryptoUtils.encrypt(this@WalletService.seed!!, pin),
                    address = this@WalletService.address!!,
                    prvKey = CryptoUtils.encrypt(this@WalletService.privKey!!, pin)
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