package dev.krishna.rickmorty.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.krishna.rickmorty.data.api.model.Filters
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.database.Bookmark
import dev.krishna.rickmorty.data.repository.CharacterRepository
import dev.krishna.rickmorty.data.repository.state.ApiResult
import dev.krishna.rickmorty.ui.adapters.RecyclerItem
import dev.krishna.rickmorty.ui.state.UIState
import dev.krishna.rickmorty.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val networkMonitor: NetworkMonitor
): ViewModel() {

    private val _uiState = MutableLiveData<UIState<PagingData<RecyclerItem>>>(UIState.Loading)
    val uiState: LiveData<UIState<PagingData<RecyclerItem>>> = _uiState

    private val _filters = MutableLiveData<Filters>(Filters())
    val filters: LiveData<Filters> = _filters

    private val _bookmarks = MutableLiveData<List<Bookmark>>()
    val bookmarks: LiveData<List<Bookmark>> = _bookmarks

    private val _searchQuery = MutableStateFlow<String?>(null)
    private var currentQuery: String? = null

    private val _isOnline = MediatorLiveData<Boolean>().apply {
        value = false
        addSource(networkMonitor) { isOnline ->
            if (value != isOnline) value = isOnline
        }
    }
    val isOnline: LiveData<Boolean> = _isOnline

    init {
        _isOnline.observeForever { isOnline ->
            if (isOnline) {
                loadCharacters(true)
            } else {
                loadCharacters(false)
            }
        }
        observeBookmarks()
        setupSearchDebouncer()
    }

    fun loadCharacters(isOnline: Boolean) {
        _uiState.value = UIState.Loading
        viewModelScope.launch {
            _filters.value?.let { filters ->
                val result = withContext(Dispatchers.IO) {
                    characterRepository.getCharacters(
                        name = filters.name,
                        status = filters.status,
                        species = filters.species,
                        isOnline = isOnline
                    )
                }
                result.observeForever { apiResult ->
                    when (apiResult) {
                        is ApiResult.Success -> {
                            val mappedData = apiResult.data.map { character ->
                                RecyclerItem.CharacterItem(
                                    character,
                                    bookmarks.value?.any { it.characterId == character.id } ?: false
                                ) as RecyclerItem
                            }
                            _uiState.postValue(UIState.Success(mappedData))
                        }
                        is ApiResult.Error -> {
                            _uiState.postValue(UIState.Error(apiResult.exception.message.toString()))
                        }
                    }
                }
            }
        }
    }

    fun applyFilters(newFilters: Filters) {
        _filters.value = newFilters
        loadCharacters(isOnline.value?: false)
    }

    fun clearFilters() {
        _filters.value = Filters()
        loadCharacters(isOnline.value ?: false)
    }

    private fun observeBookmarks() {
        characterRepository.getBookmarks().observeForever {
            _bookmarks.postValue(it)
        }
    }

    fun toggleBookmark(character: RickMortyCharacter) {
        viewModelScope.launch(Dispatchers.IO) {
            characterRepository.toggleBookmark(character)
        }
    }

    fun setSearchQuery(query: String?) {
        viewModelScope.launch {
            if (query.isNullOrEmpty()){
                currentQuery = null
                updateFiltersWithSearchQuery(null)
            }
            _searchQuery.emit(query)
        }
    }

    private fun setupSearchDebouncer() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { query ->
                    query.isNullOrBlank() || query.length >= 3
                }
                .collect { query ->
                    currentQuery = query
                    updateFiltersWithSearchQuery(query)
                }
        }
    }


    private fun updateFiltersWithSearchQuery(query: String?) {
        val currentFilters = _filters.value ?: Filters()
        val newFilters = currentFilters.copy(name = query)
        _filters.value = newFilters
        loadCharacters(isOnline.value ?: false)
    }
}