package com.labs.barnaton.feature.movies.ui.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MovieRepository,
) : ViewModel() {

    val movieId: Int = checkNotNull(savedStateHandle[ARG_MOVIE_ID]) {
        "Missing required argument: $ARG_MOVIE_ID"
    }

    val reviews: Flow<PagingData<Review>> =
        repository.getMovieReviewsPaged(movieId).cachedIn(viewModelScope)

    companion object {
        const val ARG_MOVIE_ID = "movieId"
    }
}
