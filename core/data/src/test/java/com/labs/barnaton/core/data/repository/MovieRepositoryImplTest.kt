package com.labs.barnaton.core.data.repository

import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.core.network.api.MoviesService
import com.labs.barnaton.core.network.response.GenreItemResponse
import com.labs.barnaton.core.network.response.GenresResponse
import com.labs.barnaton.core.network.response.MovieDetailResponse
import com.labs.barnaton.core.network.response.MovieItemResponse
import com.labs.barnaton.core.network.response.ListResponse
import com.labs.barnaton.core.network.response.ReviewItemResponse
import com.labs.barnaton.core.network.response.VideoItemResponse
import com.labs.barnaton.core.network.response.VideosResponse
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class MovieRepositoryImplTest {

    private val moviesService: MoviesService = mockk()
    private val repository = MovieRepositoryImpl(moviesService)

    @Test
    fun getGenresReturnsMappedGenres() = runTest {
        coEvery { moviesService.getGenres() } returns GenresResponse(
            genres = listOf(GenreItemResponse(28, "Action"), GenreItemResponse(18, "Drama")),
        )

        val result = repository.getGenres()

        assertTrue(result is Resource.Success)
        val genres = (result as Resource.Success).data
        assertEquals(2, genres.size)
        assertEquals("Action", genres[0].name)
        assertEquals("Drama", genres[1].name)
    }

    @Test
    fun getGenresReturnsNoInternetOnIOException() = runTest {
        coEvery { moviesService.getGenres() } throws IOException("Timeout")

        val result = repository.getGenres()

        assertTrue(result is Resource.Error)
        assertEquals(ErrorType.NoInternet, (result as Resource.Error).errorType)
    }

    @Test
    fun getNowPlayingMoviesReturnsMappedMovies() = runTest {
        coEvery { moviesService.getNowPlayingMovies() } returns ListResponse(
            page = 1,
            results = listOf(MovieItemResponse(id = 10, title = "Dune")),
            totalPages = 1,
            totalResults = 1,
        )

        val result = repository.getNowPlayingMovies()

        val movies = (result as Resource.Success).data
        assertEquals(1, movies.size)
        assertEquals("Dune", movies.first().title)
    }

    @Test
    fun searchMoviesReturnsMappedResults() = runTest {
        coEvery { moviesService.searchMovies(query = "matrix") } returns ListResponse(
            page = 1,
            results = listOf(MovieItemResponse(id = 603, title = "The Matrix")),
            totalPages = 1,
            totalResults = 1,
        )

        val result = repository.searchMovies("matrix")

        val movies = (result as Resource.Success).data
        assertEquals(1, movies.size)
        assertEquals("The Matrix", movies.first().title)
    }

    @Test
    fun getMovieDetailReturnsMappedDetail() = runTest {
        coEvery { moviesService.getMovieDetail(550) } returns MovieDetailResponse(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = "An insomniac office worker...",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "1999-10-15",
            runtime = 139,
            voteAverage = 8.4,
            voteCount = 28000,
            genres = listOf(GenreItemResponse(18, "Drama")),
            status = "Released",
        )

        val result = repository.getMovieDetail(550)

        val detail = (result as Resource.Success).data
        assertEquals("Fight Club", detail.title)
        assertEquals(139, detail.runtimeMinutes)
        assertEquals(1, detail.genres.size)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", detail.backdropUrl)
    }

    @Test
    fun getMovieDetailReturnsServerErrorOnHttp503() = runTest {
        val http503 = HttpException(
            Response.error<Any>(503, "Service Unavailable".toResponseBody("text/plain".toMediaType())),
        )
        coEvery { moviesService.getMovieDetail(550) } throws http503

        val result = repository.getMovieDetail(550)

        assertTrue(result is Resource.Error)
        val error = (result as Resource.Error).errorType
        assertTrue(error is ErrorType.ServerError)
        assertEquals(503, (error as ErrorType.ServerError).code)
    }

    @Test
    fun getMovieTrailersFiltersAndSortsYouTubeTrailers() = runTest {
        coEvery { moviesService.getMovieVideos(550) } returns VideosResponse(
            id = 550,
            results = listOf(
                VideoItemResponse("1", "vimeo_key", "Vimeo Trailer", "Vimeo", "Trailer", official = true),
                VideoItemResponse("2", "bts_key", "Behind the Scenes", "YouTube", "Behind the Scenes", official = true),
                VideoItemResponse("3", "teaser_key", "Official Teaser", "YouTube", "Teaser", official = true),
                VideoItemResponse("4", "fan_trailer", "Fan Trailer", "YouTube", "Trailer", official = false),
                VideoItemResponse("5", "official_trailer", "Official Trailer", "YouTube", "Trailer", official = true),
                VideoItemResponse("6", "", "Broken Key", "YouTube", "Trailer", official = true),
            ),
        )

        val result = repository.getMovieTrailers(550)

        val trailers = (result as Resource.Success).data
        assertEquals(3, trailers.size)
        assertEquals("official_trailer", trailers[0].key)
        assertEquals("fan_trailer", trailers[1].key)
        assertEquals("teaser_key", trailers[2].key)
    }

    @Test
    fun getMovieReviewsPreviewReturnsMappedReviews() = runTest {
        coEvery { moviesService.getMovieReviews(movieId = 550, page = 1) } returns ListResponse(
            page = 1,
            results = listOf(
                ReviewItemResponse(id = "r1", author = "Alice", content = "Great!", createdAt = "2024-01-01"),
            ),
            totalPages = 1,
            totalResults = 1,
        )

        val result = repository.getMovieReviewsPreview(550)

        val reviews = (result as Resource.Success).data
        assertEquals(1, reviews.size)
        assertEquals("Alice", reviews.first().author)
    }

    @Test(expected = CancellationException::class)
    fun getGenresRethrowsCancellation() = runTest {
        coEvery { moviesService.getGenres() } throws CancellationException("Job cancelled")

        repository.getGenres()
    }
}
