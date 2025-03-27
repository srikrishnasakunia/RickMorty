package dev.krishna.rickmorty.utils

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.card.MaterialCardView
import dev.krishna.rickmorty.R
import java.util.Locale

@BindingAdapter("characterImage")
fun ImageView.setCharacterImage(url: String?) {
    if (url.isNullOrEmpty().not()) {
        Glide.with(this)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.ic_placeholder_character)
            .error(R.drawable.ic_error)
            .into(this)
    }
}

@BindingAdapter("cardBorderTint")
fun MaterialCardView.setCardBorderTint(status: String?) {
    val color = when (status?.lowercase(Locale.ROOT)) {
        "alive" -> ContextCompat.getColor(context, R.color.green_alive)
        "dead" -> ContextCompat.getColor(context, R.color.red_dead)
        else -> ContextCompat.getColor(context, R.color.gray_unknown)
    }

    // Set stroke color and width
    this.strokeColor = color
    this.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_width)
}

@BindingAdapter("bookmarkIcon")
fun ImageView.setBookmarkIcon(isBookmarked: Boolean) {
    val resId = if (isBookmarked) {
        R.drawable.ic_bookmark_filled
    } else {
        R.drawable.ic_bookmark_outline
    }
    setImageResource(resId)
}


@BindingAdapter("statusBasedBackgroundColor")
fun MaterialCardView.setStatusBasedBackgroundColorBinding(status: String?) {
    val backgroundColor = when (status?.lowercase(Locale.ROOT)) {
        "alive" -> ContextCompat.getColor(context, R.color.green_alive)
        "dead" -> ContextCompat.getColor(context, R.color.red_dead)
        else -> ContextCompat.getColor(context, R.color.gray_unknown)
    }


    val strokeColor = when (status?.lowercase(Locale.ROOT)) {
        "dead" -> ContextCompat.getColor(context, R.color.white)
        else -> ContextCompat.getColor(context, R.color.black)
    }

    val borderWidth = 5

    val drawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(backgroundColor)
        setStroke(borderWidth, strokeColor)
        cornerRadius = 20f
    }

    this.background = drawable

}


@BindingAdapter("statusBasedBG")
fun FrameLayout.setStatusBasedBGBinding(status: String?) {
    val color = when (status?.lowercase(Locale.ROOT)) {
        "alive" -> ContextCompat.getColor(context, R.color.green_alive_light)
        "dead" -> ContextCompat.getColor(context, R.color.red_dead_light)
        else -> ContextCompat.getColor(context, R.color.gray_unknown_light)
    }

    this.setBackgroundColor(color)
}

@BindingAdapter("statusBasedExpandedBG")
fun MaterialCardView.setStatusBasedExpandedBGBinding(status: String?) {
    val color = when (status?.lowercase(Locale.ROOT)) {
        "alive" -> ContextCompat.getColor(context, R.color.green_alive_light)
        "dead" -> ContextCompat.getColor(context, R.color.red_dead_light)
        else -> ContextCompat.getColor(context, R.color.gray_unknown_light)
    }

    this.setCardBackgroundColor(color)
}

