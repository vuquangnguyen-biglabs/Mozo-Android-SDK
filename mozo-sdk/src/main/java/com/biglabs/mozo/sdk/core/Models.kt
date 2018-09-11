package com.biglabs.mozo.sdk.core

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
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
            val phoneNumber: String,
            val fullName: String,
            val accessToken: String? = null,
            val refreshToken: String? = null
    )

    @Entity
    data class Profile(
            @NonNull @PrimaryKey(autoGenerate = true) var id: Long = 0L,
            @Embedded
            val exchangeInfo: ExchangeInfo? = null,
            @Embedded
            val settings: Settings? = null,
            val status: String? = null,
            val userId: String,
            @Embedded
            val walletInfo: WalletInfo
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

    data class WalletInfo(
            val encryptSeedPhrase: String,
            val address: String? = null,
            val privateKey: String? = null
    )
}