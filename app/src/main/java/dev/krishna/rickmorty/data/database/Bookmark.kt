package dev.krishna.rickmorty.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val characterId: Int,
    val name: String,
    val image: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val species: String,
    val episodeUrls: List<String>? = null
)
