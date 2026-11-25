package com.labs.barnaton.feature.movies.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.barnaton.core.model.Genre
import com.labs.barnaton.feature.movies.databinding.ContentAdapterGenreBinding
import kotlin.math.abs

class GenreAdapter(
    private val onItemClick: (Genre) -> Unit,
) : ListAdapter<Genre, GenreAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContentAdapterGenreBinding.inflate(
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
        private val binding: ContentAdapterGenreBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(genre: Genre) {
            val theme = getGenreTheme(genre.id)
            with(binding) {
                tvGenreName.text = genre.name
                colorIndicator.backgroundTintList = ColorStateList.valueOf(theme.boldColor)
                cardGenre.setCardBackgroundColor(ColorStateList.valueOf(theme.bgSoft))
                cardGenre.strokeColor = theme.strokeColor
                ivChevron.imageTintList = ColorStateList.valueOf(theme.boldColor)
                root.setOnClickListener { onItemClick(genre) }
            }
        }
    }

    private data class GenreTheme(
        val bgSoft: Int,
        val boldColor: Int,
        val strokeColor: Int,
    )

    private fun getGenreTheme(genreId: Int): GenreTheme {
        return GENRE_THEMES[genreId] ?: PALETTE[abs(genreId) % PALETTE.size]
    }

    companion object {
        private fun theme(bgHex: String, boldHex: String, strokeHex: String) = GenreTheme(
            bgSoft = Color.parseColor(bgHex),
            boldColor = Color.parseColor(boldHex),
            strokeColor = Color.parseColor(strokeHex),
        )

        private val PALETTE = listOf(
            theme("#FEF2F2", "#EF4444", "#FEE2E2"),
            theme("#FFF7ED", "#F97316", "#FFEDD5"),
            theme("#FEFCE8", "#F59E0B", "#FEF08A"),
            theme("#ECFDF5", "#10B981", "#D1FAE5"),
            theme("#ECFEFF", "#06B6D4", "#CFFAFE"),
            theme("#F0F9FF", "#0284C7", "#E0F2FE"),
            theme("#EEF2FF", "#6366F1", "#E0E7FF"),
            theme("#F5F3FF", "#8B5CF6", "#EDE9FE"),
            theme("#FDF2F8", "#EC4899", "#FCE7F3"),
            theme("#F0FDFA", "#14B8A6", "#CCFBF1"),
            theme("#FDF4FF", "#D946EF", "#F5D0FE"),
        )

        private val GENRE_THEMES = mapOf(
            28 to theme("#FEF2F2", "#EF4444", "#FEE2E2"),    // Action
            12 to theme("#FFF7ED", "#F97316", "#FFEDD5"),    // Adventure
            16 to theme("#F5F3FF", "#8B5CF6", "#EDE9FE"),    // Animation
            35 to theme("#FEFCE8", "#F59E0B", "#FEF08A"),    // Comedy
            80 to theme("#F8FAFC", "#64748B", "#E2E8F0"),    // Crime
            99 to theme("#ECFDF5", "#10B981", "#D1FAE5"),    // Documentary
            18 to theme("#EEF2FF", "#6366F1", "#E0E7FF"),    // Drama
            10751 to theme("#ECFEFF", "#06B6D4", "#CFFAFE"), // Family
            14 to theme("#FAF5FF", "#A855F7", "#F3E8FF"),    // Fantasy
            36 to theme("#F5F5F4", "#B45309", "#E7E5E4"),    // History
            27 to theme("#FFF1F2", "#E11D48", "#FFE4E6"),    // Horror
            10402 to theme("#FDF2F8", "#EC4899", "#FCE7F3"), // Music
            9648 to theme("#F0F9FF", "#0284C7", "#E0F2FE"),  // Mystery
            10749 to theme("#FDF4FF", "#D946EF", "#F5D0FE"), // Romance
            878 to theme("#F0F9FF", "#3B82F6", "#DBEAFE"),   // Sci-Fi
            10770 to theme("#F0FDFA", "#14B8A6", "#CCFBF1"), // TV Movie
            53 to theme("#FFF7ED", "#EA580C", "#FFEDD5"),    // Thriller
            10752 to theme("#F1F5F9", "#475569", "#CBD5E1"), // War
            37 to theme("#FEFCE8", "#B45309", "#FEF08A"),    // Western
        )

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Genre>() {
            override fun areItemsTheSame(oldItem: Genre, newItem: Genre) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Genre, newItem: Genre) = oldItem == newItem
        }
    }
}
