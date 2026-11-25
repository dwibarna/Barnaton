package com.labs.barnaton.core.data.mapper

import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.core.network.response.GenreItemResponse
import com.labs.barnaton.core.network.response.MovieDetailResponse
import com.labs.barnaton.core.network.response.MovieItemResponse
import com.labs.barnaton.core.network.response.ReviewItemResponse
import com.labs.barnaton.core.network.response.VideoItemResponse
import java.io.IOException
import retrofit2.HttpException

object MovieMapper {

    private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

    fun posterUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return "${IMAGE_BASE_URL}w500$path"
    }

    fun backdropUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return "${IMAGE_BASE_URL}w780$path"
    }

    fun avatarUrl(path: String?): String? {
        val cleanPath = path?.trim()
        if (cleanPath.isNullOrEmpty()) return null
        return when {
            cleanPath.startsWith("/http://") || cleanPath.startsWith("/https://") -> cleanPath.removePrefix("/")
            cleanPath.startsWith("http://") || cleanPath.startsWith("https://") -> cleanPath
            else -> "${IMAGE_BASE_URL}w185$cleanPath"
        }
    }

    fun mapGenre(response: GenreItemResponse): Genre {
        return Genre(id = response.id, name = response.name)
    }

    fun mapMovie(response: MovieItemResponse): Movie {
        return Movie(
            id = response.id,
            title = response.title,
            overview = response.overview,
            posterUrl = posterUrl(response.posterPath),
            backdropUrl = backdropUrl(response.backdropPath),
            releaseDate = response.releaseDate,
            voteAverage = response.voteAverage,
            voteCount = response.voteCount,
        )
    }

    fun mapMovieDetail(response: MovieDetailResponse): MovieDetail {
        return MovieDetail(
            id = response.id,
            title = response.title,
            tagline = response.tagline ?: "",
            overview = response.overview ?: "",
            posterUrl = posterUrl(response.posterPath),
            backdropUrl = backdropUrl(response.backdropPath),
            releaseDate = response.releaseDate ?: "",
            runtimeMinutes = response.runtime?.takeIf { it > 0 },
            voteAverage = response.voteAverage,
            voteCount = response.voteCount,
            genres = response.genres.map { mapGenre(it) },
            status = response.status ?: "",
        )
    }

    fun mapReview(response: ReviewItemResponse): Review {
        val authorName = response.author.ifBlank {
            response.authorDetails?.username.orEmpty().ifBlank { "Anonymous" }
        }
        return Review(
            id = response.id,
            author = authorName,
            username = response.authorDetails?.username?.takeIf { it.isNotBlank() },
            avatarUrl = avatarUrl(response.authorDetails?.avatarPath),
            rating = response.authorDetails?.rating,
            content = response.content,
            createdAt = response.createdAt,
        )
    }

    fun mapYouTubeTrailers(videos: List<VideoItemResponse>): List<MovieVideo> {
        return videos
            .filter { video ->
                video.site.equals("YouTube", ignoreCase = true) &&
                    (video.type.equals("Trailer", ignoreCase = true) ||
                        video.type.equals("Teaser", ignoreCase = true)) &&
                    video.key.isNotBlank()
            }
            .map { video ->
                MovieVideo(
                    id = video.id,
                    key = video.key,
                    name = video.name,
                    site = video.site,
                    type = video.type,
                    official = video.official,
                )
            }
            .sortedWith(
                compareByDescending<MovieVideo> { it.type.equals("Trailer", ignoreCase = true) }
                    .thenByDescending { it.official },
            )
    }

    fun mapError(throwable: Throwable): ErrorType {
        return when (throwable) {
            is IOException -> ErrorType.NoInternet
            is HttpException -> when (val code = throwable.code()) {
                401 -> ErrorType.Unauthorized
                404 -> ErrorType.NotFound
                in 400..499 -> ErrorType.ClientError(code, throwable.message())
                in 500..599 -> ErrorType.ServerError(code, throwable.message())
                else -> ErrorType.Unknown(throwable.message())
            }
            else -> ErrorType.Unknown(throwable.localizedMessage)
        }
    }
}
