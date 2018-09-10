package com.biglabs.mozo.sdk.core

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.biglabs.mozo.sdk.core.dao.ProfileDao
import com.biglabs.mozo.sdk.core.dao.UserInfoDao

@Database(entities = [Models.UserInfo::class, Models.Profile::class], version = 1, exportSchema = false)
abstract class MozoDatabase : RoomDatabase() {
    abstract fun profile(): ProfileDao
    abstract fun userInfo(): UserInfoDao

    companion object {
        private var INSTANCE: MozoDatabase? = null

        fun getInstance(context: Context): MozoDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, MozoDatabase::class.java, "mozo.db").build()
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}