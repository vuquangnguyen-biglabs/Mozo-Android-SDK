package com.biglabs.mozo.sdk.core

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import kotlin.collections.ArrayList

object Models {

    /* Database models */
    @Entity
    data class AnonymousUserInfo(
            @NonNull @PrimaryKey var id: Long = 0L,
            val userId: String,
            val balance: Long = 0L,
            val accessToken: String? = null,
            val refreshToken: String? = null
    )

    @Entity
    data class UserInfo(
            @NonNull @PrimaryKey var id: Long = 0L,
            val userId: String,
            val phoneNumber: String? = null,
            val fullName: String? = null
    )

    @Entity(indices = [Index(value = ["id", "userId"], unique = true)])
    data class Profile(
            @PrimaryKey
            var id: Long = 0L,
            val userId: String,
            val status: String? = null,
            @Embedded
            var exchangeInfo: ExchangeInfo? = null,
            @Embedded
            val settings: Settings? = null,
            @Embedded
            var walletInfo: WalletInfo? = null
    )

    data class ExchangeInfo(
            val apiKey: String,
            val depositAddress: String? = null,
            val exchangeId: String? = null,
            val exchangePlatform: String? = null,
            var exchangeSecret: String? = null
    )

    data class Settings(
            val notificationThreshold: Int
    )

    @Suppress("SpellCheckingInspection")
    data class WalletInfo(
            var encryptSeedPhrase: String? = null,
            var offchainAddress: String? = null,
            var privateKey: String? = null
    )

    /* API services models */
    data class Contact(
            val id: Long,
            val name: String,
            val soloAddress: String
    )

    data class BalanceInfo(
            val balance: BigDecimal,
            val symbol: String?,
            val decimals: Int,
            val contractAddress: String?
    ) {
        fun balanceDisplay(): BigDecimal =
                balance.divide(Math.pow(10.0, decimals.toDouble()).toBigDecimal())
    }

    class TransactionAddress(
            val addresses: ArrayList<String> = arrayListOf()
    )

    class TransactionAddressOutput(
            val addresses: ArrayList<String> = arrayListOf(),
            val value: BigDecimal
    )

    data class TransactionRequest(
            val inputs: ArrayList<TransactionAddress> = arrayListOf(),
            val outputs: ArrayList<TransactionAddressOutput> = arrayListOf()
    )

    data class TransactionResponseData(
            val hash: String? = null,
            val fees: Double,
            val inputs: ArrayList<TransactionAddress>,
            val outputs: ArrayList<TransactionAddressOutput>,
            val data: String,
            @SerializedName("double_spend")
            val doubleSpend: Boolean,
            @SerializedName("gas_price")
            val gasPrice: Double,
            @SerializedName("gas_limit")
            val gasLimit: Double
    )

    @Suppress("SpellCheckingInspection")
    data class TransactionResponse(
            val tx: TransactionResponseData,
            @SerializedName("tosign")
            val toSign: ArrayList<String>,
            var signatures: ArrayList<String>,
            @SerializedName("pubkeys")
            var publicKeys: ArrayList<String>,
            val nonce: Long
    )
}