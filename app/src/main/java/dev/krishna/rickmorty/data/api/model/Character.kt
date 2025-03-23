package dev.krishna.rickmorty.data.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val created: String
): Parcelable

typealias RickMortyCharacter = Character