package com.biglabs.mozo.sdk.core

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

object Models {

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
            val fullName: String? = null,
            val accessToken: String? = null,
            val refreshToken: String? = null
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
}