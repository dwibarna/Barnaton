package com.labs.barnaton.feature.movies.ui.home

import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.feature.movies.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()

    private val sampleMovies = listOf(
        Movie(1, "Dune", "Sci-fi", null, null, "2024-03-01", 8.2, 5000),
    )
    private val sampleGenres = listOf(Genre(28, "Action"), Genre(35, "Comedy"))

    @Test
    fun loadHomeEmitsSuccess() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returns Resource.Success(sampleMovies)
        coEvery { repository.getGenres() } returns Resource.Success(sampleGenres)

        val viewModel = HomeViewModel(repository)
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(HomeUiState.Success(sampleMovies, sampleGenres), viewModel.uiState.value)
    }

    @Test
    fun loadHomeEmitsSuccessWhenOnlyGenresSucceed() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returns Resource.Error(ErrorType.ServerError(500))
        coEvery { repository.getGenres() } returns Resource.Success(sampleGenres)

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertEquals(HomeUiState.Success(emptyList(), sampleGenres), viewModel.uiState.value)
    }

    @Test
    fun loadHomeEmitsEmptyWhenBothEmpty() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returns Resource.Success(emptyList())
        coEvery { repository.getGenres() } returns Resource.Success(emptyList())

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertEquals(HomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun loadHomeEmitsErrorAndRecoversOnRetry() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returnsMany listOf(
            Resource.Error(ErrorType.NoInternet),
            Resource.Success(sampleMovies),
        )
        coEvery { repository.getGenres() } returnsMany listOf(
            Resource.Error(ErrorType.NoInternet),
            Resource.Success(sampleGenres),
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        assertEquals(HomeUiState.Error(ErrorType.NoInternet), viewModel.uiState.value)

        viewModel.loadHome()
        advanceUntilIdle()
        assertEquals(HomeUiState.Success(sampleMovies, sampleGenres), viewModel.uiState.value)
    }

    @Test
    fun searchEmitsResultsAndResetsWhenCleared() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returns Resource.Success(sampleMovies)
        coEvery { repository.getGenres() } returns Resource.Success(sampleGenres)
        coEvery { repository.searchMovies("dune") } returns Resource.Success(sampleMovies)

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("dune")
        advanceTimeBy(450L)
        advanceUntilIdle()
        assertEquals(SearchUiState.Success(sampleMovies), viewModel.searchState.value)

        viewModel.onSearchQueryChanged("")
        assertEquals(SearchUiState.Idle, viewModel.searchState.value)
    }

    @Test
    fun searchEmitsEmptyWhenNoMatches() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getNowPlayingMovies() } returns Resource.Success(sampleMovies)
        coEvery { repository.getGenres() } returns Resource.Success(sampleGenres)
        coEvery { repository.searchMovies("zzzz") } returns Resource.Success(emptyList())

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("zzzz", immediate = true)
        advanceUntilIdle()
        assertEquals(SearchUiState.Empty, viewModel.searchState.value)
    }
}
