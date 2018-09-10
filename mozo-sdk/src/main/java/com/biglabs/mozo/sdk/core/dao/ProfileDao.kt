package com.biglabs.mozo.sdk.core.dao

import android.arch.persistence.room.*
import com.biglabs.mozo.sdk.core.entities.Profiles

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(profile: Profiles)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(profiles: List<Profiles>)

    @Query("SELECT * FROM Profiles WHERE id = :id")
    fun get(id: Long): Profiles

    @Query("SELECT * FROM Profiles")
    fun getAll(): List<Profiles>

    @Update
    fun update(profile: Profiles)

    @Delete
    fun delete(profile: Profiles)

    @Query("DELETE from Profiles")
    fun deleteAll()
}