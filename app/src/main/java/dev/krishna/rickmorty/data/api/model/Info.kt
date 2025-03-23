package dev.krishna.rickmorty.data.api.model

data class Info(
    val count: Int,
    val page: Int,
    val next: String?,
    val prev: String?
)
