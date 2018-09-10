package com.biglabs.mozo.sdk.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class Profiles(
        @NonNull @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        val seed: String,
        val address: String,
        val prvKey: String,
        var exchangeSecret: String? = null
)