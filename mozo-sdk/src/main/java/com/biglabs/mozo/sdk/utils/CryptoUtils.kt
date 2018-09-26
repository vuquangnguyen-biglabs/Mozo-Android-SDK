package com.biglabs.mozo.sdk.utils

import android.util.Base64
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.cryptonode.jncryptor.AES256JNCryptor
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

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

        @JvmStatic
        fun serializeSignature(signature: Sign.SignatureData): String {
            val totalLength = 6 + signature.r.size + signature.s.size
            val result = ByteArray(totalLength)

            result[0] = 0x30
            result[1] = (totalLength - 2).toByte()
            result[2] = 0x02
            result[3] = signature.r.size.toByte()

            signature.r.mapIndexed { index, byte ->
                result[index + 4] = byte
            }

            val offset = signature.r.size + 4
            result[offset] = 0x02
            result[offset + 1] = signature.s.size.toByte()

            signature.s.mapIndexed { index, byte ->
                result[offset + 2 + index] = byte
            }

            return Numeric.toHexString(result)
        }
    }
}