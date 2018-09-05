package com.biglabs.mozo.sdk.common

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.biglabs.mozo.sdk.entities.Movies

@Database(entities = [Movies::class], version = 1, exportSchema = false)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun moviesDataDao(): MoviesDataDao

    companion object {
        private var INSTANCE: MoviesDatabase? = null

        fun getInstance(context: Context): MoviesDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, MoviesDatabase::class.java, "movies.db").build()
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}