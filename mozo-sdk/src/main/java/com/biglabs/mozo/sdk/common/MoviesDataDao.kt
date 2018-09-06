package com.biglabs.mozo.sdk.common

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.biglabs.mozo.sdk.entities.Movies

@Dao
interface MoviesDataDao {
    @Insert(onConflict = REPLACE)
    fun insertOnlySingleMovie(movies: Movies)

    @Insert
    fun insertMultipleMovies(moviesList: List<Movies>)

    @Query("SELECT * FROM Movies WHERE id = :movieId")
    fun fetchOneMoviesByMovieId(movieId: Long): Movies

    @Query("SELECT * FROM Movies")
    fun fetchMovies(): List<Movies>

    @Update
    fun updateMovie(movies: Movies)

    @Delete
    fun deleteMovie(movies: Movies)

    @Query("DELETE from Movies")
    fun deleteMovies()
}