package com.labs.barnaton.feature.movies.util

import android.content.Context
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.labs.barnaton.core.model.ApiErrorException
import com.labs.barnaton.core.model.ErrorType
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.ui.base.StateView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object Util {

    fun loadPoster(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.placeholder_poster)
            error(R.drawable.placeholder_poster)
        }
    }

    fun loadBackdrop(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.placeholder_backdrop)
            error(R.drawable.placeholder_backdrop)
        }
    }

    fun loadAvatar(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
        }
    }

    fun getErrorTitle(context: Context, errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NoInternet -> context.getString(R.string.error_no_internet_title)
            ErrorType.Unauthorized -> context.getString(R.string.error_unauthorized_title)
            ErrorType.NotFound -> context.getString(R.string.error_not_found_title)
            is ErrorType.ServerError -> context.getString(R.string.error_server_title, errorType.code)
            is ErrorType.ClientError -> context.getString(R.string.error_client_title, errorType.code)
            is ErrorType.Unknown -> context.getString(R.string.error_unknown_title)
        }
    }

    fun getErrorMessage(context: Context, errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NoInternet -> context.getString(R.string.error_no_internet_message)
            ErrorType.Unauthorized -> context.getString(R.string.error_unauthorized_message)
            ErrorType.NotFound -> context.getString(R.string.error_not_found_message)
            is ErrorType.ServerError -> context.getString(R.string.error_server_message)
            is ErrorType.ClientError -> errorType.message?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.error_client_message)
            is ErrorType.Unknown -> errorType.message?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.error_unknown_message)
        }
    }

    fun extractErrorType(throwable: Throwable): ErrorType {
        return if (throwable is ApiErrorException) {
            throwable.errorType
        } else {
            ErrorType.Unknown(throwable.localizedMessage)
        }
    }

    fun <T : Any, VH : RecyclerView.ViewHolder> bindPagingState(
        adapter: PagingDataAdapter<T, VH>,
        lifecycleOwner: LifecycleOwner,
        recyclerView: RecyclerView,
        swipeRefresh: SwipeRefreshLayout,
        stateView: StateView,
        emptyTitle: String,
        emptyMessage: String,
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    val refresh = loadStates.refresh
                    swipeRefresh.isRefreshing = refresh is LoadState.Loading && adapter.itemCount > 0

                    when (refresh) {
                        is LoadState.Loading -> {
                            if (adapter.itemCount == 0) {
                                recyclerView.isVisible = false
                                stateView.showLoading()
                            }
                        }
                        is LoadState.Error -> {
                            recyclerView.isVisible = false
                            val errorType = extractErrorType(refresh.error)
                            stateView.showError(
                                title = getErrorTitle(stateView.context, errorType),
                                message = getErrorMessage(stateView.context, errorType),
                                onRetry = { adapter.retry() },
                            )
                        }
                        is LoadState.NotLoading -> {
                            val isEmpty = loadStates.append.endOfPaginationReached && adapter.itemCount == 0
                            if (isEmpty) {
                                recyclerView.isVisible = false
                                stateView.showEmpty(emptyTitle, emptyMessage)
                            } else {
                                stateView.hide()
                                recyclerView.isVisible = true
                            }
                        }
                    }
                }
            }
        }
    }
}
