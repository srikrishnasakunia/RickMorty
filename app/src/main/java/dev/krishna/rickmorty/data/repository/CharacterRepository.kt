package dev.krishna.rickmorty.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.krishna.rickmorty.data.api.CharacterPagingSource
import dev.krishna.rickmorty.data.api.RickMortyApiService
import dev.krishna.rickmorty.data.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton
import dev.krishna.rickmorty.data.api.model.Character
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.database.Bookmark
import dev.krishna.rickmorty.data.repository.state.ApiResult

@Singleton
class CharacterRepository @Inject constructor(
    private val apiService: RickMortyApiService,
    private val appDatabase: AppDatabase
) {

    suspend fun getCharacters(
        name: String?,
        status: String?,
        species: String?
    ): LiveData<ApiResult<PagingData<Character>>> = liveData{
        try {
            val pager = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {
                    CharacterPagingSource(
                        apiService = apiService,
                        name = name,
                        status = status,
                        species = species
                    )
                }
            ).flow
            pager.collect{ value ->
                emit(ApiResult.Success(value))
            }
        } catch (e:Exception) {
            Log.e("CharacterRepository", "Error fetching characters: ${e.message}")
            emit(ApiResult.Error(e))
        }
    }

    fun getBookmarks(): LiveData<List<Bookmark>> {
        return appDatabase.bookmarkDao().getAllBookmarks()
    }

    suspend fun toggleBookmark(character: Character) {
        val bookmark = Bookmark(
            characterId = character.id,
            name = character.name,
            image = character.image
        )
        if (appDatabase.bookmarkDao().isBookmarked(character.id)) {
            appDatabase.bookmarkDao().deleteBookmark(bookmark)
        } else {
            appDatabase.bookmarkDao().insertBookmark(bookmark)
        }
    }

    suspend fun getCharacterDetails(characterId: Int): ApiResult<Character>{
        try {
            val characterDetails = apiService.getCharacterDetails(characterId)
            Log.d("CharacterRepository", "getCharacterDetails: $characterDetails")
            return ApiResult.Success(characterDetails)
        } catch (e: Exception) {
            return ApiResult.Error(e)
        }
    }

    suspend fun getEpisodesData(episodeUrls: List<String>): ApiResult<List<Episode>> {
        if (episodeUrls.isEmpty()) {
            return ApiResult.Success(emptyList())
        }
        try {
            val episodeIds = episodeUrls.map { it.substringAfterLast("/") }

            val episodeResponse = if (episodeIds.size == 1) {
                listOf(apiService.getSingleEpisodes(episodeIds.first()))
            } else
                apiService.getMultipleEpisodes(episodeIds.joinToString(","))
            return ApiResult.Success(episodeResponse)
        } catch (e:Exception) {
            return ApiResult.Error(e)
        }
    }

}