package com.labs.barnaton.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.labs.barnaton.core.data.mapper.MovieMapper
import com.labs.barnaton.core.data.paging.MovieReviewsPagingSource
import com.labs.barnaton.core.data.paging.MoviesPagingSource
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Resource
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.core.network.api.MoviesService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val moviesService: MoviesService,
) : MovieRepository {

    override suspend fun getGenres(): Resource<List<Genre>> {
        return try {
            val genres = moviesService.getGenres().genres.map { MovieMapper.mapGenre(it) }
            Resource.Success(genres)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override suspend fun getNowPlayingMovies(): Resource<List<Movie>> {
        return try {
            val movies = moviesService.getNowPlayingMovies().results.map { MovieMapper.mapMovie(it) }
            Resource.Success(movies)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override suspend fun searchMovies(query: String): Resource<List<Movie>> {
        return try {
            val movies = moviesService.searchMovies(query = query).results.map { MovieMapper.mapMovie(it) }
            Resource.Success(movies)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override fun getMoviesByGenre(genreId: Int): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = { MoviesPagingSource(moviesService, genreId) },
        ).flow
    }

    override suspend fun getMovieDetail(movieId: Int): Resource<MovieDetail> {
        return try {
            val detail = MovieMapper.mapMovieDetail(moviesService.getMovieDetail(movieId))
            Resource.Success(detail)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override suspend fun getMovieTrailers(movieId: Int): Resource<List<MovieVideo>> {
        return try {
            val trailers = MovieMapper.mapYouTubeTrailers(moviesService.getMovieVideos(movieId).results)
            Resource.Success(trailers)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override suspend fun getMovieReviewsPreview(movieId: Int): Resource<List<Review>> {
        return try {
            val reviews = moviesService.getMovieReviews(movieId = movieId, page = 1)
                .results
                .map { MovieMapper.mapReview(it) }
            Resource.Success(reviews)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Resource.Error(MovieMapper.mapError(e), e)
        }
    }

    override fun getMovieReviewsPaged(movieId: Int): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = { MovieReviewsPagingSource(moviesService, movieId) },
        ).flow
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 4
    }
}
