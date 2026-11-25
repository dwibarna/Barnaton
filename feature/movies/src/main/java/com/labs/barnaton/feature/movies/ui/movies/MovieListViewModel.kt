package com.labs.barnaton.feature.movies.ui.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class MovieListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MovieRepository,
) : ViewModel() {

    val genreId: Int = checkNotNull(savedStateHandle[ARG_GENRE_ID]) {
        "Missing required argument: $ARG_GENRE_ID"
    }

    val movies: Flow<PagingData<Movie>> =
        repository.getMoviesByGenre(genreId).cachedIn(viewModelScope)

    companion object {
        const val ARG_GENRE_ID = "genreId"
    }
}
