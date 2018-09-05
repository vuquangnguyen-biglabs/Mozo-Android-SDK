package com.biglabs.mozo.sdk.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Movies(@PrimaryKey var movieId: String?, var movieName: String?)