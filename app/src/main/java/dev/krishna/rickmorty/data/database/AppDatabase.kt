package dev.krishna.rickmorty.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Bookmark::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object{
        const val DATABASE_NAME = "app_database"
    }
}