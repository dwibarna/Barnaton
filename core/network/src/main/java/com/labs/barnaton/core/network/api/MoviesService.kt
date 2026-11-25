package com.labs.barnaton.core.network.api

import com.labs.barnaton.core.network.response.GenresResponse
import com.labs.barnaton.core.network.response.MovieDetailResponse
import com.labs.barnaton.core.network.response.MovieItemResponse
import com.labs.barnaton.core.network.response.ListResponse
import com.labs.barnaton.core.network.response.ReviewItemResponse
import com.labs.barnaton.core.network.response.VideosResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesService {

    @GET("genre/movie/list")
    suspend fun getGenres(): GenresResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): ListResponse<MovieItemResponse>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): ListResponse<MovieItemResponse>

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
    ): ListResponse<MovieItemResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
    ): MovieDetailResponse

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int,
    ): ListResponse<ReviewItemResponse>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
    ): VideosResponse
}
