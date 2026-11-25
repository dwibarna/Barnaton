package com.labs.barnaton.feature.movies.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labs.barnaton.core.data.repository.MovieRepository
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.core.model.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MovieDetailUiState {
    data object Loading : MovieDetailUiState()
    data class Success(
        val detail: MovieDetail,
        val trailer: MovieVideo?,
        val previewReviews: List<Review>,
    ) : MovieDetailUiState()
    data class Error(val errorType: ErrorType) : MovieDetailUiState()
}

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    val movieId: Int = checkNotNull(savedStateHandle[ARG_MOVIE_ID]) {
        "Missing required argument: $ARG_MOVIE_ID"
    }

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            coroutineScope {
                val detailJob = async { repository.getMovieDetail(movieId) }
                val trailersJob = async { repository.getMovieTrailers(movieId) }
                val reviewsJob = async { repository.getMovieReviewsPreview(movieId) }

                when (val detailResult = detailJob.await()) {
                    is Resource.Error -> {
                        _uiState.value = MovieDetailUiState.Error(detailResult.errorType)
                    }
                    is Resource.Success -> {
                        val trailer = (trailersJob.await() as? Resource.Success)
                            ?.data
                            ?.firstOrNull()
                        val previewReviews = (reviewsJob.await() as? Resource.Success)
                            ?.data
                            ?.take(MAX_PREVIEW_REVIEWS)
                            .orEmpty()

                        _uiState.value = MovieDetailUiState.Success(
                            detail = detailResult.data,
                            trailer = trailer,
                            previewReviews = previewReviews,
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_MOVIE_ID = "movieId"
        const val MAX_PREVIEW_REVIEWS = 3
    }
}
