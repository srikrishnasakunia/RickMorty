package dev.krishna.rickmorty.ui.adapters

import dev.krishna.rickmorty.data.api.model.RickMortyCharacter

sealed class RecyclerItem {
    data class CharacterItem(val character: RickMortyCharacter, val isBookmarked: Boolean) : RecyclerItem()
    data class HeaderItem(val name: String) : RecyclerItem()
}