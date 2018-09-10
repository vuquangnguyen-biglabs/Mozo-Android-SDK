package com.biglabs.mozo.sdk.common

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.biglabs.mozo.sdk.entities.Profiles

@Database(entities = [Profiles::class], version = 1, exportSchema = false)
abstract class MozoDatabase : RoomDatabase() {
    abstract fun profile(): ProfileDao

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