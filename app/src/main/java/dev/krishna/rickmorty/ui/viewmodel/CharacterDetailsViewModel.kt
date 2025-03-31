package dev.krishna.rickmorty.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.repository.CharacterRepository
import dev.krishna.rickmorty.data.repository.state.ApiResult
import dev.krishna.rickmorty.ui.state.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UIState<RickMortyCharacter>>(UIState.Loading)
    val uiState: LiveData<UIState<RickMortyCharacter>> = _uiState

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> = _episodes

    private val _characters = MutableLiveData<RickMortyCharacter>()
    val characters: LiveData<RickMortyCharacter> = _characters

    val isExpanded = MutableLiveData(false)

    fun loadCharacterDetails(characterId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val characterDetails = characterRepository.getCharacterDetails(characterId)
                Log.d("CharacterDetailsViewModel", "loadCharacterDetails: $characterDetails")
                when (characterDetails) {
                    is ApiResult.Success -> {
                        _characters.postValue(characterDetails.data)
                        _uiState.postValue(UIState.Success(characterDetails.data))
                        loadEpisodes(characterDetails.data.episode)
                    }

                    is ApiResult.Error -> {
                        _uiState.postValue(UIState.Error(characterDetails.exception.message.toString()))
                    }
                }
            } catch (e: Exception) {
                _uiState.postValue(UIState.Error(e.message.toString()))
            }
        }
    }

    private fun loadEpisodes(episodeUrls: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val episodes = characterRepository.getEpisodesData(episodeUrls)
                Log.d("CharacterDetailsViewModel", "loadEpisodes: $episodes")
                when (episodes) {
                    is ApiResult.Success -> {
                        _episodes.postValue(episodes.data)
                    }

                    is ApiResult.Error -> {
                        _uiState.postValue(UIState.Error(episodes.exception.message.toString()))
                    }
                }
            } catch (e: Exception) {
                _uiState.postValue(UIState.Error(e.message.toString()))
            }
        }
    }

    fun toggleExpand() {
        isExpanded.value = !(isExpanded.value ?: false)
    }
}