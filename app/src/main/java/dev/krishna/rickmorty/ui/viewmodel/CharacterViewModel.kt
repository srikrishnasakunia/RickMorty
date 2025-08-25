package dev.krishna.rickmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.krishna.rickmorty.data.api.model.Filters
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.repository.CharacterRepository
import dev.krishna.rickmorty.utils.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val bookmarks: Flow<Set<Int>> = characterRepository.getBookmarks().map { list ->
        list.map { it.characterId }.toSet()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val characters: Flow<PagingData<RickMortyCharacter>> = combine(
        filters,
        searchQuery.debounce(300),
        isOnline
    ) { currentFilters, query, online ->
        Pair(currentFilters.copy(name = query.ifEmpty { null }), online)
    }.distinctUntilChanged()
        .flatMapLatest { (finalFilters, online) ->
            characterRepository.getCharacters(finalFilters, online)
        }
        .combine(bookmarks) { pagingData, bookmarkedIds ->
            pagingData.map { character ->
                character.copy(isBookmarked = bookmarkedIds.contains(character.id))
            }
        }
        .cachedIn(viewModelScope)

    fun applyFilters(newFilters: Filters) {
        _filters.value = newFilters
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(character: RickMortyCharacter) {
        viewModelScope.launch {
            characterRepository.toggleBookmark(character)
        }
    }
}