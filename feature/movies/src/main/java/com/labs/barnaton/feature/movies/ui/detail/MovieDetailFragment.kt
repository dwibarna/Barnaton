package com.labs.barnaton.feature.movies.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.labs.barnaton.core.model.MovieDetail
import com.labs.barnaton.core.model.MovieVideo
import com.labs.barnaton.core.model.Review
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.FragmentMovieDetailBinding
import com.labs.barnaton.feature.movies.ui.base.BaseFragment
import com.labs.barnaton.feature.movies.util.Util
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment<FragmentMovieDetailBinding>() {

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()

    private lateinit var reviewPreviewAdapter: ReviewPreviewAdapter
    private var youTubePlayer: YouTubePlayer? = null
    private var currentTrailerKey: String? = null
    private var pendingTrailerKey: String? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentMovieDetailBinding {
        return FragmentMovieDetailBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        reviewPreviewAdapter = ReviewPreviewAdapter()
        with(binding) {
            rvReviewsPreview.layoutManager = LinearLayoutManager(requireContext())
            rvReviewsPreview.adapter = reviewPreviewAdapter
            rvReviewsPreview.isNestedScrollingEnabled = false

            viewLifecycleOwner.lifecycle.addObserver(youtubePlayerView)
            val playerOptions = IFramePlayerOptions.Builder(requireContext().applicationContext)
                .controls(1)
                .rel(0)
                .build()
            youtubePlayerView.initialize(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        this@MovieDetailFragment.youTubePlayer = youTubePlayer
                        pendingTrailerKey?.let { key ->
                            if (currentTrailerKey != key) {
                                currentTrailerKey = key
                                youTubePlayer.cueVideo(key, 0f)
                            }
                        }
                    }
                },
                true,
                playerOptions,
            )
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    with(binding) {
                        when (state) {
                            MovieDetailUiState.Loading -> {
                                scrollContent.isVisible = false
                                stateView.showLoading()
                            }
                            is MovieDetailUiState.Error -> {
                                scrollContent.isVisible = false
                                stateView.showError(
                                    title = Util.getErrorTitle(requireContext(), state.errorType),
                                    message = Util.getErrorMessage(requireContext(), state.errorType),
                                    onRetry = { viewModel.loadMovieDetail() },
                                )
                            }
                            is MovieDetailUiState.Success -> {
                                stateView.hide()
                                scrollContent.isVisible = true
                                showDetail(state.detail)
                                showTrailer(state.trailer)
                                showReviews(state.previewReviews, state.detail.title)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDetail(detail: MovieDetail) {
        with(binding) {
            Util.loadBackdrop(ivBackdrop, detail.backdropUrl ?: detail.posterUrl)
            Util.loadPoster(ivPoster, detail.posterUrl)

            tvTitle.text = detail.title
            tvTagline.isVisible = detail.tagline.isNotBlank()
            tvTagline.text = detail.tagline
            tvRating.text = getString(R.string.label_rating_with_votes, detail.voteAverage, detail.voteCount)

            val metaParts = mutableListOf<String>()
            if (detail.releaseDate.isNotBlank()) metaParts.add(detail.releaseDate)
            detail.runtimeMinutes?.let { minutes ->
                val hours = minutes / 60
                val mins = minutes % 60
                val text = if (hours > 0) {
                    getString(R.string.label_runtime_hours_mins, hours, mins)
                } else {
                    getString(R.string.label_runtime_mins, mins)
                }
                metaParts.add(text)
            }
            tvMetadata.text = metaParts.joinToString(" \u2022 ").ifBlank { "-" }

            chipGroupGenres.removeAllViews()
            val chipBg = ContextCompat.getColorStateList(requireContext(), R.color.surface_card)
            val chipStroke = ContextCompat.getColorStateList(requireContext(), R.color.outline_border)
            val chipText = ContextCompat.getColor(requireContext(), R.color.text_primary)
            detail.genres.forEach { genre ->
                val chip = Chip(requireContext()).apply {
                    text = genre.name
                    chipBackgroundColor = chipBg
                    chipStrokeColor = chipStroke
                    chipStrokeWidth = resources.displayMetrics.density
                    setTextColor(chipText)
                    isClickable = false
                    isCheckable = false
                }
                chipGroupGenres.addView(chip)
            }

            tvOverview.text = detail.overview.ifBlank { getString(R.string.label_no_synopsis) }
        }
    }

    private fun showTrailer(trailer: MovieVideo?) {
        with(binding) {
            val hasTrailer = trailer != null
            cardTrailerPlayer.isVisible = hasTrailer
            btnOpenYouTube.isVisible = hasTrailer
            tvEmptyTrailer.isVisible = !hasTrailer

            if (trailer != null) {
                pendingTrailerKey = trailer.key
                val player = youTubePlayer
                if (player != null && currentTrailerKey != trailer.key) {
                    currentTrailerKey = trailer.key
                    player.cueVideo(trailer.key, 0f)
                }
                btnOpenYouTube.setOnClickListener {
                    openYouTube(trailer)
                }
            }
        }
    }

    private fun showReviews(reviews: List<Review>, movieTitle: String) {
        with(binding) {
            val hasReviews = reviews.isNotEmpty()
            rvReviewsPreview.isVisible = hasReviews
            btnSeeAllReviews.isVisible = hasReviews
            tvEmptyReviews.isVisible = !hasReviews

            reviewPreviewAdapter.submitList(reviews)
            btnSeeAllReviews.setOnClickListener {
                val action = MovieDetailFragmentDirections.actionMovieDetailToReviews(
                    movieId = args.movieId,
                    movieTitle = movieTitle,
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun openYouTube(trailer: MovieVideo) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${trailer.key}")))
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trailer.youtubeWatchUrl)))
            } catch (_: ActivityNotFoundException) {
                Snackbar.make(binding.root, R.string.label_youtube_not_installed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun clearViews() {
        youTubePlayer = null
        currentTrailerKey = null
        pendingTrailerKey = null
        binding.rvReviewsPreview.adapter = null
    }
}
