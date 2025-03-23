package dev.krishna.rickmorty.utils

import dev.krishna.rickmorty.data.api.model.Character

fun List<Character>.groupBySpecies(): Map<String?, List<Character>> {
    return groupBy { it.species }
        .toSortedMap(compareBy { it })
        .mapValues { entry -> entry.value.sortedBy { it.name } }
}
