package com.labs.barnaton.feature.movies.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.feature.movies.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()
    private val movieId = 157336

    private val sampleDetail = MovieDetail(
        id = movieId,
        title = "Interstellar",
        tagline = "Mankind was born on Earth.",
        overview = "Explorers travel through a wormhole.",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate = "2014-11-05",
        runtimeMinutes = 169,
        voteAverage = 8.4,
        voteCount = 34000,
        genres = listOf(Genre(878, "Science Fiction")),
        status = "Released",
    )

    private val sampleTrailer = MovieVideo(
        id = "vid-1",
        key = "zSWdZVtXT7E",
        name = "Official Trailer",
        site = "YouTube",
        type = "Trailer",
        official = true,
    )

    private fun sampleReviews(count: Int): List<Review> = (1..count).map { index ->
        Review(
            id = "r-$index",
            author = "Author $index",
            username = "user$index",
            avatarUrl = null,
            rating = 8.0,
            content = "Review content $index",
            createdAt = "2024-01-0$index",
        )
    }

    @Test
    fun loadMovieDetailEmitsSuccessWithTrailerAndCappedReviews() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getMovieDetail(movieId) } returns Resource.Success(sampleDetail)
            coEvery { repository.getMovieTrailers(movieId) } returns Resource.Success(listOf(sampleTrailer))
            coEvery { repository.getMovieReviewsPreview(movieId) } returns Resource.Success(sampleReviews(5))

            val viewModel = MovieDetailViewModel(
                savedStateHandle = SavedStateHandle(mapOf(MovieDetailViewModel.ARG_MOVIE_ID to movieId)),
                repository = repository,
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value as MovieDetailUiState.Success
            assertEquals(sampleDetail, state.detail)
            assertEquals(sampleTrailer, state.trailer)
            assertEquals(MovieDetailViewModel.MAX_PREVIEW_REVIEWS, state.previewReviews.size)
        }

    @Test
    fun loadMovieDetailStillSucceedsWhenTrailerAndReviewsFail() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getMovieDetail(movieId) } returns Resource.Success(sampleDetail)
            coEvery { repository.getMovieTrailers(movieId) } returns Resource.Error(ErrorType.ServerError(500))
            coEvery { repository.getMovieReviewsPreview(movieId) } returns Resource.Error(ErrorType.NoInternet)

            val viewModel = MovieDetailViewModel(
                savedStateHandle = SavedStateHandle(mapOf(MovieDetailViewModel.ARG_MOVIE_ID to movieId)),
                repository = repository,
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value as MovieDetailUiState.Success
            assertEquals(sampleDetail, state.detail)
            assertNull(state.trailer)
            assertEquals(emptyList<Review>(), state.previewReviews)
        }

    @Test
    fun loadMovieDetailEmitsErrorAndRecoversOnRetry() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getMovieDetail(movieId) } returnsMany listOf(
                Resource.Error(ErrorType.NotFound),
                Resource.Success(sampleDetail),
            )
            coEvery { repository.getMovieTrailers(movieId) } returns Resource.Success(emptyList())
            coEvery { repository.getMovieReviewsPreview(movieId) } returns Resource.Success(emptyList())

            val viewModel = MovieDetailViewModel(
                savedStateHandle = SavedStateHandle(mapOf(MovieDetailViewModel.ARG_MOVIE_ID to movieId)),
                repository = repository,
            )
            advanceUntilIdle()
            assertEquals(MovieDetailUiState.Error(ErrorType.NotFound), viewModel.uiState.value)

            viewModel.loadMovieDetail()
            advanceUntilIdle()
            val state = viewModel.uiState.value as MovieDetailUiState.Success
            assertEquals(sampleDetail, state.detail)
        }
}
