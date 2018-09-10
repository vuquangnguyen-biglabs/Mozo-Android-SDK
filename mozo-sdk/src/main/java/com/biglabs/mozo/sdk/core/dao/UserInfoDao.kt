package com.biglabs.mozo.sdk.core.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.biglabs.mozo.sdk.core.Models.UserInfo

@Dao
interface UserInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(userInfo: UserInfo)

    @Query("SELECT * FROM UserInfo WHERE id = :id")
    fun get(id: Long = 0L): UserInfo?
}