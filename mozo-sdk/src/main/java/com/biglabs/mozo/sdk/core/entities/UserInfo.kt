package com.biglabs.mozo.sdk.core.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class UserInfo(
        @NonNull @PrimaryKey var id: Long = 0L,
        val userId: String,
        val phoneNumber: String,
        val fullName: String
)