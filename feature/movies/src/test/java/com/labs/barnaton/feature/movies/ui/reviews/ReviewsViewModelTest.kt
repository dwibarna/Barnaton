package com.labs.barnaton.feature.movies.ui.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.feature.movies.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: MovieRepository = mockk()

    @Test
    fun reviewsEmitsPagedReviewsForMovieId() = runTest {
        val expectedReviews = listOf(
            Review("r1", "Alice", "alice", null, 9.0, "Awesome!", "2024-05-01"),
        )
        val pagingSourceFactory = expectedReviews.asPagingSourceFactory()
        every { repository.getMovieReviewsPaged(550) } returns Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = pagingSourceFactory,
        ).flow

        val viewModel = ReviewsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ReviewsViewModel.ARG_MOVIE_ID to 550)),
            repository = repository,
        )

        val snapshot = viewModel.reviews.asSnapshot()
        assertEquals(550, viewModel.movieId)
        assertEquals(expectedReviews, snapshot)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenMovieIdMissing() {
        ReviewsViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repository,
        )
    }
}
