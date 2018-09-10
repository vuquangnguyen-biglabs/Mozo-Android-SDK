package com.biglabs.mozo.sdk.core

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

object Models {
    @Entity
    data class UserInfo(
            @NonNull @PrimaryKey var id: Long = 0L,
            val userId: String,
            val phoneNumber: String,
            val fullName: String
    )

    @Entity
    data class Profile(
            @NonNull @PrimaryKey(autoGenerate = true) var id: Long = 0L,
            val userId: String,
            val seed: String,
            val address: String,
            val prvKey: String,
            var exchangeSecret: String? = null
    )
}