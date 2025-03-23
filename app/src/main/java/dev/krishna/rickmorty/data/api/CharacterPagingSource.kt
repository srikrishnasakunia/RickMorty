package dev.krishna.rickmorty.data.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.krishna.rickmorty.data.api.model.Character

class CharacterPagingSource(
    private val apiService: RickMortyApiService,
    private val name: String?,
    private val status: String?,
    private val species: String?
) : PagingSource<Int, Character>() {
    override fun getRefreshKey(state: PagingState<Int, Character>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getCharacters(page, name, status, species)
            LoadResult.Page(
                data = response.results,
                prevKey = if (response.info.prev != null) page - 1 else null,
                nextKey = if (response.info.next != null) page + 1 else null
            )
        } catch (e: Exception) {
            Log.e("CharacterPagingSource", "Error fetching characters: ${e.message}")
            LoadResult.Error(e)
        }
    }
}