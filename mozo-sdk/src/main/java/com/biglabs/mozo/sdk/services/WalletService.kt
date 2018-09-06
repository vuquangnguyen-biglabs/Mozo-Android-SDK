package com.biglabs.mozo.sdk.services

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.CryptoUtils
import com.biglabs.mozo.sdk.utils.PermissionUtils
import com.estimote.android_ketchup.kotlin_goodness.toHexString
import kotlinx.coroutines.experimental.launch
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom


internal class WalletService private constructor() {

    fun createWallet() {
        MozoSDK.context?.let {
            if (PermissionUtils.requestPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                executeCreateWallet()
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
            SecurityActivity.start(it)
            launch {

                val mnemonic = MnemonicUtils.generateMnemonic(
                        SecureRandom().generateSeed(16)
                )

                val key = DeterministicKeyChain
                        .builder()
                        .seed(DeterministicSeed(mnemonic, null, "", System.nanoTime()))
                        .build()
                        .getKeyByPath(HDUtils.parsePath(ETH_FIRST_ADDRESS_PATH), true)
                val privKey = key.privKey.toString(16)

                // Web3j
                val credentials = Credentials.create(privKey)
                Log.e("vu", "mnemonic: $mnemonic")
                Log.e("vu", "address: ${credentials.address}")
                Log.e("vu", "publicKey: ${key.pubKey.toHexString()}")
                Log.e("vu", "privateKey: $privKey")

                /*
                launch {
                    val movies = Movies(name = "Civil War")
                    MoviesDatabase.getInstance(it).moviesDataDao().insertOnlySingleMovie(movies)

                    val result = MoviesDatabase.getInstance(it).moviesDataDao().fetchMovies()
                    Log.e("vu", "movies: " + result)

                    launch(UI) {
                        Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
                    }
                }
                */
            }
        }
    }

    companion object {
        const val ETH_FIRST_ADDRESS_PATH = "M/44H/60H/0H/0/0"

        @Volatile
        private var INSTANCE: WalletService? = null

        fun getInstance(): WalletService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: WalletService()
                }
    }
}