package com.labs.barnaton.feature.movies.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.FragmentMovieListBinding
import com.labs.barnaton.feature.movies.ui.base.BaseFragment
import com.labs.barnaton.feature.movies.ui.base.PagingLoadStateAdapter
import com.labs.barnaton.feature.movies.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieListFragment : BaseFragment<FragmentMovieListBinding>() {

    private val viewModel: MovieListViewModel by viewModels()
    private lateinit var movieAdapter: MoviePagingAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentMovieListBinding {
        return FragmentMovieListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        movieAdapter = MoviePagingAdapter { movie ->
            val action = MovieListFragmentDirections.actionMovieListToMovieDetail(
                movieId = movie.id,
                movieTitle = movie.title,
            )
            findNavController().navigate(action)
        }

        val footerAdapter = PagingLoadStateAdapter { movieAdapter.retry() }
        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position >= movieAdapter.itemCount) 2 else 1
                }
            }
        }

        with(binding) {
            rvMovies.layoutManager = gridLayoutManager
            rvMovies.adapter = movieAdapter.withLoadStateFooter(footer = footerAdapter)
            swipeRefresh.setOnRefreshListener { movieAdapter.refresh() }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.movies.collectLatest { pagingData ->
                    movieAdapter.submitData(pagingData)
                }
            }
        }

        Util.bindPagingState(
            adapter = movieAdapter,
            lifecycleOwner = viewLifecycleOwner,
            recyclerView = binding.rvMovies,
            swipeRefresh = binding.swipeRefresh,
            stateView = binding.stateView,
            emptyTitle = getString(R.string.empty_movies_title),
            emptyMessage = getString(R.string.empty_movies_message),
        )
    }

    override fun clearViews() {
        binding.rvMovies.adapter = null
    }
}
