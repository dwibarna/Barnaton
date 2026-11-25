package com.labs.barnaton.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.labs.barnaton.core.data.mapper.MovieMapper
import com.labs.barnaton.core.model.ApiErrorException
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.core.network.api.MoviesService
import kotlinx.coroutines.CancellationException

class MovieReviewsPagingSource(
    private val moviesService: MoviesService,
    private val movieId: Int,
) : PagingSource<Int, Review>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
        val page = params.key ?: 1
        return try {
            val response = moviesService.getMovieReviews(movieId = movieId, page = page)
            val items = response.results.map { MovieMapper.mapReview(it) }
            val nextKey = if (items.isEmpty() || page >= response.totalPages) null else page + 1
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            LoadResult.Error(ApiErrorException(MovieMapper.mapError(e), e))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }
}
