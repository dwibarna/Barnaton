package com.labs.barnaton.core.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenresResponse(
    @SerialName("genres") val genres: List<GenreItemResponse> = emptyList(),
)

@Serializable
data class GenreItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

@Serializable
data class ListResponse<T>(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class MovieItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
)

@Serializable
data class MovieDetailResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("genres") val genres: List<GenreItemResponse> = emptyList(),
    @SerialName("status") val status: String? = null,
)

@Serializable
data class ReviewItemResponse(
    @SerialName("id") val id: String,
    @SerialName("author") val author: String = "",
    @SerialName("author_details") val authorDetails: AuthorDetailsResponse? = null,
    @SerialName("content") val content: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class AuthorDetailsResponse(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("rating") val rating: Double? = null,
)

@Serializable
data class VideosResponse(
    @SerialName("id") val id: Int,
    @SerialName("results") val results: List<VideoItemResponse> = emptyList(),
)

@Serializable
data class VideoItemResponse(
    @SerialName("id") val id: String,
    @SerialName("key") val key: String,
    @SerialName("name") val name: String = "",
    @SerialName("site") val site: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("official") val official: Boolean = false,
)
