package com.labs.barnaton.core.data.paging

import androidx.paging.PagingSource
import com.labs.barnaton.core.model.ApiErrorException
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.network.api.MoviesService
import com.labs.barnaton.core.network.response.AuthorDetailsResponse
import com.labs.barnaton.core.network.response.ListResponse
import com.labs.barnaton.core.network.response.ReviewItemResponse
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class MovieReviewsPagingSourceTest {

    private val moviesService: MoviesService = mockk()
    private val movieId = 550

    @Test
    fun loadReturnsMappedReviewsAndAvatarUrl() = runTest {
        val review = ReviewItemResponse(
            id = "rev-1",
            author = "Jane",
            authorDetails = AuthorDetailsResponse(
                username = "jane_d",
                avatarPath = "/https://secure.gravatar.com/avatar/abc.jpg",
                rating = 9.0,
            ),
            content = "Fantastic movie!",
            createdAt = "2024-05-01T10:00:00.000Z",
        )
        coEvery {
            moviesService.getMovieReviews(movieId = movieId, page = 1)
        } returns ListResponse(page = 1, results = listOf(review), totalPages = 2, totalResults = 25)

        val pagingSource = MovieReviewsPagingSource(moviesService, movieId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("https://secure.gravatar.com/avatar/abc.jpg", page.data.first().avatarUrl)
        assertEquals(9.0, page.data.first().rating)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun loadReturnsEmptyPageWhenNoReviews() = runTest {
        coEvery {
            moviesService.getMovieReviews(movieId = movieId, page = 1)
        } returns ListResponse(page = 1, results = emptyList(), totalPages = 0, totalResults = 0)

        val pagingSource = MovieReviewsPagingSource(moviesService, movieId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test
    fun loadMapsIOExceptionToNoInternet() = runTest {
        coEvery {
            moviesService.getMovieReviews(movieId = movieId, page = 1)
        } throws IOException("Offline")

        val pagingSource = MovieReviewsPagingSource(moviesService, movieId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val error = (result as PagingSource.LoadResult.Error).throwable as ApiErrorException
        assertEquals(ErrorType.NoInternet, error.errorType)
    }

    @Test
    fun loadMapsHttp404ToNotFound() = runTest {
        val http404 = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody("text/plain".toMediaType())),
        )
        coEvery {
            moviesService.getMovieReviews(movieId = movieId, page = 1)
        } throws http404

        val pagingSource = MovieReviewsPagingSource(moviesService, movieId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val error = (result as PagingSource.LoadResult.Error).throwable as ApiErrorException
        assertEquals(ErrorType.NotFound, error.errorType)
    }

    @Test(expected = CancellationException::class)
    fun loadRethrowsCancellation() = runTest {
        coEvery {
            moviesService.getMovieReviews(movieId = movieId, page = 1)
        } throws CancellationException("Cancelled")

        val pagingSource = MovieReviewsPagingSource(moviesService, movieId)
        pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )
    }
}
