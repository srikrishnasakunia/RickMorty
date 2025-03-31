package dev.krishna.rickmorty.data.api.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@JsonClass(generateAdapter = true)
data class Character(
    val id: Int,
    val name: String,
    val status: String?,
    val species: String?,
    val type: String?,
    val gender: String,
    val origin: Origin,
    val location: Origin,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String,
    val isBookmarked: Boolean = false
): Parcelable

typealias RickMortyCharacter = Character