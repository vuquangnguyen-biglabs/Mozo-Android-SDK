package com.biglabs.mozo.sdk.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class Movies(@NonNull @PrimaryKey(autoGenerate = true) var id: Long = 0L, var name: String?)