package dev.krishna.rickmorty.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.krishna.rickmorty.utils.MoshiConverter

@Database(entities = [Bookmark::class], version = 1, exportSchema = false)
@TypeConverters(MoshiConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object{
        const val DATABASE_NAME = "app_database"
    }
}