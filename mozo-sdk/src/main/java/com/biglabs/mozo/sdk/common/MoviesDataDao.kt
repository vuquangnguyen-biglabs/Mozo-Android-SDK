package com.biglabs.mozo.sdk.common

import android.arch.persistence.room.*
import com.biglabs.mozo.sdk.entities.Movies

@Dao
interface MoviesDataDao {
    @Insert
    fun insertOnlySingleMovie(movies: Movies)

    @Insert
    fun insertMultipleMovies(moviesList: List<Movies>)

    @Query("SELECT * FROM Movies WHERE movieId = :movieId")
    fun fetchOneMoviesbyMovieId(movieId: Int): Movies

    @Update
    fun updateMovie(movies: Movies)

    @Delete
    fun deleteMovie(movies: Movies)
}