package com.labs.barnaton.feature.movies.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.barnaton.core.model.Movie
import com.labs.barnaton.feature.movies.databinding.ContentAdapterNowPlayingBinding
import com.labs.barnaton.feature.movies.util.Util
import java.util.Locale

class NowPlayingAdapter(
    private val onItemClick: (Movie) -> Unit,
) : ListAdapter<Movie, NowPlayingAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContentAdapterNowPlayingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ContentAdapterNowPlayingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(movie: Movie) {
            with(binding) {
                tvTitle.text = movie.title
                tvReleaseDate.text = movie.releaseDate.take(4).ifBlank { "-" }
                tvRating.text = String.format(Locale.US, "%.1f", movie.voteAverage)
                Util.loadPoster(ivPoster, movie.posterUrl)
                root.setOnClickListener { onItemClick(movie) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie) = oldItem == newItem
        }
    }
}
