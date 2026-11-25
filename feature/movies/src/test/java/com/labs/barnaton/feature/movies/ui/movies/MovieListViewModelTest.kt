package com.labs.barnaton.feature.movies.ui.movies

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.Movie
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
class MovieListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val repository: MovieRepository = mockk()

    @Test
    fun moviesEmitsPagedMoviesForGenreId() = runTest {
        val expectedMovies = listOf(
            Movie(1, "Movie A", "Overview", null, null, "2024-01-01", 7.5, 100),
            Movie(2, "Movie B", "Overview", null, null, "2024-02-01", 8.0, 200),
        )
        val pagingSourceFactory = expectedMovies.asPagingSourceFactory()
        every { repository.getMoviesByGenre(28) } returns Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = pagingSourceFactory,
        ).flow

        val viewModel = MovieListViewModel(
            savedStateHandle = SavedStateHandle(mapOf(MovieListViewModel.ARG_GENRE_ID to 28)),
            repository = repository,
        )

        val snapshot = viewModel.movies.asSnapshot()
        assertEquals(28, viewModel.genreId)
        assertEquals(expectedMovies, snapshot)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenGenreIdMissing() {
        MovieListViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repository,
        )
    }
}
