package com.biglabs.mozo.sdk.utils

import android.util.Base64
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.cryptonode.jncryptor.AES256JNCryptor
import java.security.MessageDigest
import kotlin.experimental.and

internal class CryptoUtils {
    companion object {

        @JvmStatic
        @Throws(Throwable::class)
        fun encrypt(value: String, password: String): String? {
            return Base64.encodeToString(AES256JNCryptor().encryptData(
                    value.toByteArray(),
                    password.toCharArray()
            ), Base64.DEFAULT).replace("\n", "")
        }

        @JvmStatic
        @Throws(Throwable::class)
        fun decrypt(value: String, password: String): String? {
            return String(
                    AES256JNCryptor().decryptData(
                            Base64.decode(
                                    value.replace("\n", ""),
                                    Base64.DEFAULT
                            ),
                            password.toCharArray()
                    )
            )
        }

        @JvmStatic
        @Throws(Throwable::class)
        fun hashSHA(value: String): String? {
            val bytes = MessageDigest.getInstance("SHA-512").digest(value.toByteArray())
            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(Integer.toString((bytes[i].and(0xff.toByte())) + 0x100, 16).substring(1))
            }
            return sb.toString()
        }

        private const val ETH_FIRST_ADDRESS_PATH = "M/44H/60H/0H/0/0"
        @JvmStatic
        fun getFirstAddressPrivateKey(mnemonic: String): String {
            val key = DeterministicKeyChain
                    .builder()
                    .seed(DeterministicSeed(mnemonic, null, "", System.nanoTime()))
                    .build()
                    .getKeyByPath(HDUtils.parsePath(ETH_FIRST_ADDRESS_PATH), true)
            return key.privKey.toString(16)
        }
    }
}