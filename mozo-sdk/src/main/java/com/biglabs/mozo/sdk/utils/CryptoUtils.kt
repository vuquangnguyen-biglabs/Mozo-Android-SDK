package com.biglabs.mozo.sdk.utils

import android.util.Base64
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtils {
    companion object {
        private fun generateParameterSpec(p: String) = IvParameterSpec(p.repeat(16 / p.length + 1).substring(0, 16).toByteArray())

        @Throws(Throwable::class)
        private fun generateKey(password: String): ByteArray {
            val spec = PBEKeySpec(password.toCharArray(), password.toByteArray(), 65536, 256)
            val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            return f.generateSecret(spec).encoded
        }

        @Throws(Throwable::class)
        fun encrypt(input: ByteArray, key: ByteArray, spec: AlgorithmParameterSpec): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), spec)
            return cipher.doFinal(input)
        }

        @Throws(Throwable::class)
        fun decrypt(input: ByteArray, key: ByteArray, spec: AlgorithmParameterSpec): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), spec)
            return cipher.doFinal(input)
        }

        @JvmStatic
        @Throws(Throwable::class)
        fun encrypt(input: String, password: String): String {
            return String(
                    Base64.encode(
                            encrypt(
                                    input.toByteArray(charset("UTF-8")),
                                    generateKey(password),
                                    generateParameterSpec(password)
                            ),
                            Base64.DEFAULT
                    )
            )
        }

        @JvmStatic
        @Throws(Throwable::class)
        fun decrypt(input: String, password: String): String {
            return String(
                    decrypt(
                            Base64.decode(input, Base64.DEFAULT),
                            generateKey(password),
                            generateParameterSpec(password)
                    )
            )
        }
    }
}