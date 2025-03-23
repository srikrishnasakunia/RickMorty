package dev.krishna.rickmorty.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dev.krishna.rickmorty.R

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

@BindingAdapter("statustint")
fun ImageView.setStatusTint(status: String?) {
    val color = when(status?.lowercase()) {
        "alive" -> ContextCompat.getColor(context, R.color.green_alive)
        "dead" -> ContextCompat.getColor(context, R.color.red_dead)
        else -> ContextCompat.getColor(context, R.color.gray_unknown)
    }

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
