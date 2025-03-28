package dev.krishna.rickmorty.ui.viewmodel

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
): ViewModel() {

    private val _uiState = MutableLiveData<UIState<PagingData<RecyclerItem>>>(UIState.Loading)
    val uiState: MutableLiveData<UIState<PagingData<RecyclerItem>>> = _uiState

    private val _filters = MutableLiveData<Filters>(Filters())
    val filters: MutableLiveData<Filters> = _filters

    private val _bookmarks = MutableLiveData<List<Bookmark>>()
    val bookmarks: MutableLiveData<List<Bookmark>> = _bookmarks


    init {
        loadCharacters()
        observeBookmarks()
    }

    fun loadCharacters() {
        _uiState.value = UIState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            _filters.value?.let { filters ->
                characterRepository.getCharacters(
                    name = filters.name,
                    status = filters.status,
                    species = filters.species
                ).observeForever { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val mappedData = result.data.map { character ->
                                RecyclerItem.CharacterItem(character) as RecyclerItem
                            }
                            _uiState.postValue(UIState.Success(mappedData))
                        }

                        is ApiResult.Error -> {
                            _uiState.postValue(UIState.Error(result.exception.message.toString()))
                        }
                    }
                }
            }
        }
    }

    fun applyFilters(newFilters: Filters) {
        _filters.value = newFilters
        loadCharacters()
    }

    fun clearFilters(filters: Filters) {
        _filters.value = Filters()
        loadCharacters()
    }

    private fun observeBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            characterRepository.getBookmarks().observeForever {
                _bookmarks.postValue(it)
            }
        }
    }

    fun toggleBookmark(character: RickMortyCharacter) {
        viewModelScope.launch(Dispatchers.IO) {
            characterRepository.toggleBookmark(character)
        }
    }
}