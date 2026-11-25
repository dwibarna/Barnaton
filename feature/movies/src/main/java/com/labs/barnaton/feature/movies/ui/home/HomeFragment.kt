package com.labs.barnaton.feature.movies.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.FragmentHomeBinding
import com.labs.barnaton.feature.movies.ui.base.BaseFragment
import com.labs.barnaton.feature.movies.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var nowPlayingAdapter: NowPlayingAdapter
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var searchAdapter: SearchMovieAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        nowPlayingAdapter = NowPlayingAdapter { movie ->
            navigateToDetail(movie.id, movie.title)
        }
        genreAdapter = GenreAdapter { genre ->
            val action = HomeFragmentDirections.actionHomeToMovieList(
                genreId = genre.id,
                genreName = genre.name,
            )
            findNavController().navigate(action)
        }
        searchAdapter = SearchMovieAdapter { movie ->
            navigateToDetail(movie.id, movie.title)
        }

        with(binding) {
            rvNowPlaying.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvNowPlaying.adapter = nowPlayingAdapter

            rvGenres.layoutManager = GridLayoutManager(requireContext(), 2)
            rvGenres.adapter = genreAdapter
            rvGenres.isNestedScrollingEnabled = false

            rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
            rvSearchResults.adapter = searchAdapter
            rvSearchResults.isNestedScrollingEnabled = false

            etSearch.doAfterTextChanged { text ->
                val query = text?.toString().orEmpty()
                ivClearSearch.isVisible = query.isNotEmpty()
                viewModel.onSearchQueryChanged(query)
            }
            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.onSearchQueryChanged(etSearch.text?.toString().orEmpty(), immediate = true)
                    true
                } else {
                    false
                }
            }
            ivClearSearch.setOnClickListener {
                etSearch.setText("")
            }

            swipeRefresh.setOnRefreshListener {
                if (viewModel.searchState.value is SearchUiState.Idle) {
                    viewModel.loadHome()
                } else {
                    viewModel.retrySearch()
                }
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        if (viewModel.searchState.value is SearchUiState.Idle) {
                            renderHomeState(state)
                        }
                    }
                }
                launch {
                    viewModel.searchState.collect { searchState ->
                        renderSearchState(searchState)
                    }
                }
            }
        }
    }

    private fun renderHomeState(state: HomeUiState) {
        with(binding) {
            swipeRefresh.isRefreshing = false
            layoutSearchResults.isVisible = false
            when (state) {
                HomeUiState.Loading -> {
                    layoutBrowseContent.isVisible = false
                    stateView.showLoading()
                }
                HomeUiState.Empty -> {
                    layoutBrowseContent.isVisible = false
                    stateView.showEmpty(
                        title = getString(R.string.empty_genres_title),
                        message = getString(R.string.empty_genres_message),
                    )
                }
                is HomeUiState.Success -> {
                    stateView.hide()
                    layoutBrowseContent.isVisible = true
                    layoutNowPlaying.isVisible = state.nowPlaying.isNotEmpty()
                    layoutGenres.isVisible = state.genres.isNotEmpty()
                    nowPlayingAdapter.submitList(state.nowPlaying)
                    genreAdapter.submitList(state.genres)
                }
                is HomeUiState.Error -> {
                    layoutBrowseContent.isVisible = false
                    stateView.showError(
                        title = Util.getErrorTitle(requireContext(), state.errorType),
                        message = Util.getErrorMessage(requireContext(), state.errorType),
                        onRetry = { viewModel.loadHome() },
                    )
                }
            }
        }
    }

    private fun renderSearchState(state: SearchUiState) {
        with(binding) {
            when (state) {
                SearchUiState.Idle -> {
                    layoutSearchResults.isVisible = false
                    renderHomeState(viewModel.uiState.value)
                }
                SearchUiState.Loading -> {
                    swipeRefresh.isRefreshing = false
                    layoutBrowseContent.isVisible = false
                    layoutSearchResults.isVisible = false
                    stateView.showLoading()
                }
                SearchUiState.Empty -> {
                    swipeRefresh.isRefreshing = false
                    layoutBrowseContent.isVisible = false
                    layoutSearchResults.isVisible = false
                    stateView.showEmpty(
                        title = getString(R.string.empty_search_title),
                        message = getString(R.string.empty_search_message),
                    )
                }
                is SearchUiState.Success -> {
                    swipeRefresh.isRefreshing = false
                    stateView.hide()
                    layoutBrowseContent.isVisible = false
                    layoutSearchResults.isVisible = true
                    searchAdapter.submitList(state.movies)
                }
                is SearchUiState.Error -> {
                    swipeRefresh.isRefreshing = false
                    layoutBrowseContent.isVisible = false
                    layoutSearchResults.isVisible = false
                    stateView.showError(
                        title = Util.getErrorTitle(requireContext(), state.errorType),
                        message = Util.getErrorMessage(requireContext(), state.errorType),
                        onRetry = { viewModel.retrySearch() },
                    )
                }
            }
        }
    }

    private fun navigateToDetail(movieId: Int, movieTitle: String) {
        val action = HomeFragmentDirections.actionHomeToMovieDetail(
            movieId = movieId,
            movieTitle = movieTitle,
        )
        findNavController().navigate(action)
    }

    override fun clearViews() {
        with(binding) {
            rvNowPlaying.adapter = null
            rvGenres.adapter = null
            rvSearchResults.adapter = null
        }
    }
}
