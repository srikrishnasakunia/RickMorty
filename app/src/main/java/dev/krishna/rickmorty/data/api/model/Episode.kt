package dev.krishna.rickmorty.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Episode(
    @Json(name = "name") val name: String,
    @Json(name = "episode") val episodeCode: String
)