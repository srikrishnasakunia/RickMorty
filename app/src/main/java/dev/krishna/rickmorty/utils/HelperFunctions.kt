package dev.krishna.rickmorty.utils

fun parseEpisodeNumber(episodeUrl: String): String {
    val parts = episodeUrl.split("/").takeLast(2)
    return "S${parts[0]}E${parts[1]}"
}