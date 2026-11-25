package com.labs.barnaton.feature.movies.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.core.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(
        val nowPlaying: List<Movie>,
        val genres: List<Genre>,
    ) : HomeUiState()
    data class Error(val errorType: ErrorType) : HomeUiState()
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data object Empty : SearchUiState()
    data class Success(val movies: List<Movie>) : SearchUiState()
    data class Error(val errorType: ErrorType) : SearchUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private var currentQuery: String = ""
    private var searchJob: Job? = null

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            coroutineScope {
                val nowPlayingJob = async { repository.getNowPlayingMovies() }
                val genresJob = async { repository.getGenres() }

                val nowPlayingResult = nowPlayingJob.await()
                val genresResult = genresJob.await()

                if (nowPlayingResult is Resource.Error && genresResult is Resource.Error) {
                    _uiState.value = HomeUiState.Error(genresResult.errorType)
                } else {
                    val nowPlaying = (nowPlayingResult as? Resource.Success)?.data.orEmpty()
                    val genres = (genresResult as? Resource.Success)?.data.orEmpty()
                    _uiState.value = if (nowPlaying.isEmpty() && genres.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(nowPlaying = nowPlaying, genres = genres)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String, immediate: Boolean = false) {
        val trimmed = query.trim()
        if (trimmed == currentQuery && !immediate) return
        currentQuery = trimmed

        searchJob?.cancel()
        if (trimmed.isEmpty()) {
            _searchState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            if (!immediate) {
                delay(SEARCH_DEBOUNCE_MS)
            }
            _searchState.value = SearchUiState.Loading
            when (val result = repository.searchMovies(trimmed)) {
                is Resource.Success -> {
                    _searchState.value = if (result.data.isEmpty()) {
                        SearchUiState.Empty
                    } else {
                        SearchUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _searchState.value = SearchUiState.Error(result.errorType)
                }
            }
        }
    }

    fun retrySearch() {
        if (currentQuery.isNotEmpty()) {
            onSearchQueryChanged(currentQuery, immediate = true)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}
