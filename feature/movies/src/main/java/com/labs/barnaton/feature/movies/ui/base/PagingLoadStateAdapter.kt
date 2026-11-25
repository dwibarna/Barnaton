package com.labs.barnaton.feature.movies.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.barnaton.feature.movies.databinding.ContentAdapterLoadStateBinding
import com.labs.barnaton.feature.movies.util.Util

class PagingLoadStateAdapter(
    private val onRetry: () -> Unit,
) : LoadStateAdapter<PagingLoadStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val binding = ContentAdapterLoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bindItem(loadState)
    }

    inner class ViewHolder(
        private val binding: ContentAdapterLoadStateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnFooterRetry.setOnClickListener { onRetry() }
        }

        fun bindItem(loadState: LoadState) {
            with(binding) {
                layoutFooterLoading.isVisible = loadState is LoadState.Loading
                tvFooterError.isVisible = loadState is LoadState.Error
                btnFooterRetry.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    val errorType = Util.extractErrorType(loadState.error)
                    tvFooterError.text = Util.getErrorMessage(root.context, errorType)
                }
            }
        }
    }
}
