package dev.krishna.rickmorty.ui.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.database.BookmarkDao
import dev.krishna.rickmorty.utils.convertBookMarkToRickMortyCharacter

class CachedCharactersPagingSource(
    private val bookmarkDao: BookmarkDao
): PagingSource<Int, RickMortyCharacter>() {
    override fun getRefreshKey(state: PagingState<Int, RickMortyCharacter>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RickMortyCharacter> {
        val cachedCharacters = bookmarkDao.getPagedBookmarks()
        val convertedData = List(cachedCharacters.size) { index ->
            cachedCharacters[index].convertBookMarkToRickMortyCharacter()
        }

        return LoadResult.Page(
            data = convertedData,
            prevKey = null,
            nextKey = null
        )
    }

}