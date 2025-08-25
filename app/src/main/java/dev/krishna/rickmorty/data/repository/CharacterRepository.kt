package dev.krishna.rickmorty.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.krishna.rickmorty.data.api.CharacterPagingSource
import dev.krishna.rickmorty.data.api.RickMortyApiService
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.database.AppDatabase
import dev.krishna.rickmorty.data.database.Bookmark
import dev.krishna.rickmorty.data.repository.state.ApiResult
import dev.krishna.rickmorty.utils.UNKNOWN
import dev.krishna.rickmorty.utils.convertBookMarkToRickMortyCharacter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val apiService: RickMortyApiService,
    private val appDatabase: AppDatabase
) {

    fun getCharacters(
        filters: dev.krishna.rickmorty.data.api.model.Filters,
        isOnline: Boolean
    ): Flow<PagingData<RickMortyCharacter>> {
        return if (isOnline) {
            Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {
                    CharacterPagingSource(
                        apiService = apiService,
                        name = filters.name,
                        status = filters.status,
                        species = filters.species
                    )
                }
            ).flow
        } else {
            Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { appDatabase.bookmarkDao().getPagedBookmarks() }
            ).flow.map { pagingData ->
                pagingData.map { bookmark ->
                    bookmark.convertBookMarkToRickMortyCharacter()
                }
            }
        }
    }

    fun getBookmarks(): Flow<List<Bookmark>> {
        return appDatabase.bookmarkDao().getAllBookmarks()
    }

    suspend fun toggleBookmark(character: RickMortyCharacter) {
        val bookmark = Bookmark(
            characterId = character.id,
            name = character.name,
            image = character.image,
            status = character.status ?: UNKNOWN,
            species = character.species ?: UNKNOWN,
            episodeUrls = character.episode
        )
        if (appDatabase.bookmarkDao().isBookmarked(character.id)) {
            appDatabase.bookmarkDao().deleteBookmark(bookmark)
        } else {
            appDatabase.bookmarkDao().insertBookmark(bookmark)
        }
    }

    fun getCharacterDetailsFlow(characterId: Int): Flow<ApiResult<RickMortyCharacter>> = flow {
        try {
            val characterDetails = apiService.getCharacterDetails(characterId)
            emit(ApiResult.Success(characterDetails))
        } catch (e: Exception) {
            emit(ApiResult.Error(e))
        }
    }

    fun getEpisodesDataFlow(episodeUrls: List<String>): Flow<ApiResult<List<Episode>>> = flow {
        if (episodeUrls.isEmpty()) {
            emit(ApiResult.Success(emptyList()))
            return@flow
        }
        try {
            val episodeIds = episodeUrls.map { it.substringAfterLast("/") }
            val episodeResponse = apiService.getMultipleEpisodes(episodeIds.joinToString(","))
            emit(ApiResult.Success(episodeResponse))
        } catch (e: Exception) {
            emit(ApiResult.Error(e))
        }
    }
}