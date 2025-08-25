package dev.krishna.rickmorty.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import dev.krishna.rickmorty.data.repository.CharacterRepository
import dev.krishna.rickmorty.data.repository.state.ApiResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: StateFlow<Int> = savedStateHandle.getStateFlow("characterId", -1)

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded = _isExpanded.asStateFlow()

    data class CharacterDetailsUiState(
        val character: RickMortyCharacter? = null,
        val episodes: List<Episode> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CharacterDetailsUiState> = characterId.flatMapLatest { id ->
        if (id == -1) {
            flowOf(CharacterDetailsUiState(isLoading = false, error = "Character ID not found"))
        } else {
            characterRepository.getCharacterDetailsFlow(id)
                .flatMapLatest { characterResult ->
                    when (characterResult) {
                        is ApiResult.Success -> {
                            val character = characterResult.data
                            characterRepository.getEpisodesDataFlow(character.episode)
                                .map { episodesResult ->
                                    when (episodesResult) {
                                        is ApiResult.Success -> {
                                            CharacterDetailsUiState(
                                                character = character,
                                                episodes = episodesResult.data,
                                                isLoading = false
                                            )
                                        }
                                        is ApiResult.Error -> {
                                            CharacterDetailsUiState(
                                                character = character,
                                                isLoading = false,
                                                error = episodesResult.exception.message
                                            )
                                        }
                                    }
                                }
                        }
                        is ApiResult.Error -> {
                            flowOf(
                                CharacterDetailsUiState(
                                    isLoading = false,
                                    error = characterResult.exception.message
                                )
                            )
                        }
                    }
                }
        }
    }.onStart {
        emit(CharacterDetailsUiState(isLoading = true))
    }.catch { e ->
        emit(CharacterDetailsUiState(isLoading = false, error = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CharacterDetailsUiState(isLoading = true)
    )

    fun toggleExpand() {
        _isExpanded.value = !_isExpanded.value
    }
}