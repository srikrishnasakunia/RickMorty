package dev.krishna.rickmorty.utils

import dev.krishna.rickmorty.data.api.model.Origin
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.database.Bookmark

fun Bookmark.convertBookMarkToRickMortyCharacter(): RickMortyCharacter =
    RickMortyCharacter(
        id = characterId,
        name = name,
        image = image,
        status = status,
        species = species,
        episode = episodeUrls.orEmpty(),
        isBookmarked = true,
        location = Origin("",""),
        origin = Origin("",""),
        url = "",
        created = timestamp.toString(),
        type = null,
        gender = UNKNOWN
    )