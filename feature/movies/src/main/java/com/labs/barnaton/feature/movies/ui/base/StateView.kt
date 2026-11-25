package com.labs.barnaton.feature.movies.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.labs.barnaton.feature.movies.R
import com.labs.barnaton.feature.movies.databinding.ViewStateBinding

class StateView : ConstraintLayout {

    private val binding: ViewStateBinding =
        ViewStateBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    )

    fun showLoading(message: String = context.getString(R.string.label_loading)) {
        isVisible = true
        with(binding) {
            layoutLoading.isVisible = true
            layoutMessage.isVisible = false
            tvLoadingMessage.text = message
        }
    }

    fun showEmpty(title: String, message: String) {
        isVisible = true
        with(binding) {
            layoutLoading.isVisible = false
            layoutMessage.isVisible = true
            iconBadgeContainer.background.mutate().setTint(
                ContextCompat.getColor(context, R.color.surface_subtle),
            )
            ivStateIcon.setImageResource(R.drawable.ic_empty_movie)
            tvStateTitle.text = title
            tvStateMessage.text = message
            btnRetry.isVisible = false
            btnRetry.setOnClickListener(null)
        }
    }

    fun showError(title: String, message: String, onRetry: (() -> Unit)? = null) {
        isVisible = true
        with(binding) {
            layoutLoading.isVisible = false
            layoutMessage.isVisible = true
            iconBadgeContainer.background.mutate().setTint(
                ContextCompat.getColor(context, R.color.error_container),
            )
            ivStateIcon.setImageResource(R.drawable.ic_error_outline)
            tvStateTitle.text = title
            tvStateMessage.text = message
            btnRetry.isVisible = onRetry != null
            btnRetry.setOnClickListener { onRetry?.invoke() }
        }
    }

    fun hide() {
        isVisible = false
        binding.layoutLoading.isVisible = false
        binding.layoutMessage.isVisible = false
    }
}
