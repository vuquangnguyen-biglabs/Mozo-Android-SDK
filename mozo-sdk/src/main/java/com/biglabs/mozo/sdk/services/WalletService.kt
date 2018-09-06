package com.biglabs.mozo.sdk.services

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.common.MoviesDatabase
import com.biglabs.mozo.sdk.entities.Movies
import com.biglabs.mozo.sdk.ui.SecurityActivity
import com.biglabs.mozo.sdk.utils.PermissionUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
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
                val bytes = ByteArray(16 /* 12 words */)
                SecureRandom().nextBytes(bytes)
                val wallet = MnemonicUtils.generateMnemonic(bytes)

                val msg = "Wallet service init: \n wallet: $wallet"
                Log.e("vu", msg)

                launch {
                    val movies = Movies(name = "Civil War")
                    MoviesDatabase.getInstance(it).moviesDataDao().insertOnlySingleMovie(movies)

                    val result = MoviesDatabase.getInstance(it).moviesDataDao().fetchMovies()
                    Log.e("vu", "movies: " + result)

                    launch(UI) {
                        Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WalletService? = null

        fun getInstance(): WalletService =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: WalletService()
                }
    }
}