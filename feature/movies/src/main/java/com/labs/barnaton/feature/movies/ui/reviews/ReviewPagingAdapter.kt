package com.labs.barnaton.feature.movies.ui.reviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.ContentAdapterReviewBinding
import com.labs.barnaton.feature.movies.util.Util

class ReviewPagingAdapter : PagingDataAdapter<Review, ReviewItemViewHolder>(ReviewDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewItemViewHolder {
        val binding = ContentAdapterReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ReviewItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewItemViewHolder, position: Int) {
        getItem(position)?.let { holder.bindItem(it, Int.MAX_VALUE) }
    }
}

class ReviewItemViewHolder(
    private val binding: ContentAdapterReviewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(review: Review, maxLines: Int) {
        with(binding) {
            tvAuthor.text = review.author
            tvCreatedAt.text = review.createdAt.take(10).ifBlank { "-" }
            tvContent.maxLines = maxLines
            tvContent.text = review.content

            val rating = review.rating
            tvReviewRating.isVisible = rating != null
            if (rating != null) {
                tvReviewRating.text = root.context.getString(R.string.label_review_rating, rating)
            }

            Util.loadAvatar(ivAvatar, review.avatarUrl)
        }
    }
}

object ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
}
