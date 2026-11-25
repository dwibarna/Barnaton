package com.labs.barnaton.core.model

data class Genre(
    val id: Int,
    val name: String,
)

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val status: String,
)

data class Review(
    val id: String,
    val author: String,
    val username: String?,
    val avatarUrl: String?,
    val rating: Double?,
    val content: String,
    val createdAt: String,
)

data class MovieVideo(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean,
) {
    val youtubeWatchUrl: String
        get() = "https://www.youtube.com/watch?v=$key"
}
