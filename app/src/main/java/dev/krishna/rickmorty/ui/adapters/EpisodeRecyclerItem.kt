package dev.krishna.rickmorty.ui.adapters

import dev.krishna.rickmorty.data.api.model.Episode

sealed class EpisodeRecyclerItem {
    data class EpisodeItem(val episode: Episode) : EpisodeRecyclerItem()
    object EmptyPlaceholder : EpisodeRecyclerItem()

}