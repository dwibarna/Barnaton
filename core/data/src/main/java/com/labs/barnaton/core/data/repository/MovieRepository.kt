package com.labs.barnaton.core.data.repository

import androidx.paging.PagingData
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.core.model.Review
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getGenres(): Resource<List<Genre>>
    suspend fun getNowPlayingMovies(): Resource<List<Movie>>
    suspend fun searchMovies(query: String): Resource<List<Movie>>
    fun getMoviesByGenre(genreId: Int): Flow<PagingData<Movie>>
    suspend fun getMovieDetail(movieId: Int): Resource<MovieDetail>
    suspend fun getMovieTrailers(movieId: Int): Resource<List<MovieVideo>>
    suspend fun getMovieReviewsPreview(movieId: Int): Resource<List<Review>>
    fun getMovieReviewsPaged(movieId: Int): Flow<PagingData<Review>>
}
