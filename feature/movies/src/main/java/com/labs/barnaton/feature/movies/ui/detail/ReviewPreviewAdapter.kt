package com.labs.barnaton.feature.movies.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.feature.movies.databinding.ContentAdapterReviewBinding
import com.labs.barnaton.feature.movies.ui.reviews.ReviewDiffCallback
import com.labs.barnaton.feature.movies.ui.reviews.ReviewItemViewHolder

class ReviewPreviewAdapter : ListAdapter<Review, ReviewItemViewHolder>(ReviewDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewItemViewHolder {
        val binding = ContentAdapterReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ReviewItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewItemViewHolder, position: Int) {
        holder.bindItem(getItem(position), 4)
    }
}
