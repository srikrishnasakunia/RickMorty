package dev.krishna.rickmorty.data.api.model

data class CharacterResponse(
    val info: Info,
    val results: List<Character>
)
