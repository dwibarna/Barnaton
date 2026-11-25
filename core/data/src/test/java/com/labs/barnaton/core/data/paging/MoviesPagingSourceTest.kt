package com.labs.barnaton.core.data.paging

import androidx.paging.PagingSource
import com.labs.barnaton.core.model.ApiErrorException
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.network.api.MoviesService
import com.labs.barnaton.core.network.response.MovieItemResponse
import com.labs.barnaton.core.network.response.ListResponse
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

class MoviesPagingSourceTest {

    private val moviesService: MoviesService = mockk()
    private val genreId = 28

    @Test
    fun loadReturnsPageWithNextKey() = runTest {
        val item = MovieItemResponse(
            id = 101,
            title = "Inception",
            overview = "Dream heist",
            posterPath = "/inception.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2010-07-16",
            voteAverage = 8.4,
            voteCount = 35000,
        )
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 1)
        } returns ListResponse(
            page = 1,
            results = listOf(item),
            totalPages = 3,
            totalResults = 60,
        )

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("Inception", page.data.first().title)
        assertEquals("https://image.tmdb.org/t/p/w500/inception.jpg", page.data.first().posterUrl)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun loadReturnsNullNextKeyOnLastPage() = runTest {
        val item = MovieItemResponse(id = 102, title = "Tenet")
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 2)
        } returns ListResponse(page = 2, results = listOf(item), totalPages = 2, totalResults = 21)

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false),
        )

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun loadReturnsEmptyPageWhenNoResults() = runTest {
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 1)
        } returns ListResponse(page = 1, results = emptyList(), totalPages = 1, totalResults = 0)

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun loadMapsIOExceptionToNoInternet() = runTest {
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 1)
        } throws IOException("No network")

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val error = (result as PagingSource.LoadResult.Error).throwable as ApiErrorException
        assertEquals(ErrorType.NoInternet, error.errorType)
    }

    @Test
    fun loadMapsHttp401ToUnauthorized() = runTest {
        val http401 = HttpException(
            Response.error<Any>(401, "Unauthorized".toResponseBody("text/plain".toMediaType())),
        )
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 1)
        } throws http401

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        val error = (result as PagingSource.LoadResult.Error).throwable as ApiErrorException
        assertEquals(ErrorType.Unauthorized, error.errorType)
    }

    @Test
    fun loadMapsHttp500ToServerError() = runTest {
        val http500 = HttpException(
            Response.error<Any>(500, "Internal Server Error".toResponseBody("text/plain".toMediaType())),
        )
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 2)
        } throws http500

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false),
        )

        val error = (result as PagingSource.LoadResult.Error).throwable as ApiErrorException
        assertTrue(error.errorType is ErrorType.ServerError)
        assertEquals(500, (error.errorType as ErrorType.ServerError).code)
    }

    @Test(expected = CancellationException::class)
    fun loadRethrowsCancellation() = runTest {
        coEvery {
            moviesService.getMoviesByGenre(genreId = genreId, page = 1)
        } throws CancellationException("Cancelled")

        val pagingSource = MoviesPagingSource(moviesService, genreId)
        pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )
    }
}
