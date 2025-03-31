package dev.krishna.rickmorty.utils

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiConverter {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val type = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(type)

    @TypeConverter
    fun fromEpisodeUrlList(value: List<String>?): String? {
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toEpisodeUrlList(value: String?): List<String>? {
        return value?.let { adapter.fromJson(it) }
    }
}