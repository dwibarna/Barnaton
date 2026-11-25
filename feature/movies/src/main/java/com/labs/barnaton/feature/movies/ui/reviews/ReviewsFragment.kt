package com.labs.barnaton.feature.movies.ui.reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.FragmentReviewsBinding
import com.labs.barnaton.feature.movies.ui.base.BaseFragment
import com.labs.barnaton.feature.movies.ui.base.PagingLoadStateAdapter
import com.labs.barnaton.feature.movies.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewsFragment : BaseFragment<FragmentReviewsBinding>() {

    private val viewModel: ReviewsViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewPagingAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentReviewsBinding {
        return FragmentReviewsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        reviewAdapter = ReviewPagingAdapter()
        val footerAdapter = PagingLoadStateAdapter { reviewAdapter.retry() }

        with(binding) {
            rvReviews.layoutManager = LinearLayoutManager(requireContext())
            rvReviews.adapter = reviewAdapter.withLoadStateFooter(footer = footerAdapter)
            swipeRefresh.setOnRefreshListener { reviewAdapter.refresh() }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reviews.collectLatest { pagingData ->
                    reviewAdapter.submitData(pagingData)
                }
            }
        }

        Util.bindPagingState(
            adapter = reviewAdapter,
            lifecycleOwner = viewLifecycleOwner,
            recyclerView = binding.rvReviews,
            swipeRefresh = binding.swipeRefresh,
            stateView = binding.stateView,
            emptyTitle = getString(R.string.empty_reviews_title),
            emptyMessage = getString(R.string.empty_reviews_message),
        )
    }

    override fun clearViews() {
        binding.rvReviews.adapter = null
    }
}
