package dev.krishna.rickmorty.data.api.model

import androidx.annotation.Keep

@Keep
data class Filters(
    val name: String? = null,
    val status: String? = null,
    val species: String? = null
)
