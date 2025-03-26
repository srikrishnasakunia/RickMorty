package dev.krishna.rickmorty.data.api.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CharacterResponse(
    val info: Info,
    val results: List<Character>
)
