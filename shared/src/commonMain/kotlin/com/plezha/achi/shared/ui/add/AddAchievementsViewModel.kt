package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddAchievementUiState(
    val asciiCode: String = "",
    val isLoading: Boolean = false
)

sealed class NavigationEvent {
    object NavigateToCreatePack : NavigationEvent()
    object NavigateToCreateAchievement : NavigationEvent()
}

class AddAchievementsViewModel(
    private val achievementPackRepository: AchievementPackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddAchievementUiState())
    val uiState: StateFlow<AddAchievementUiState> = _uiState.asStateFlow()

    private val _messageChannel = Channel<String>()
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

    private val _navigationChannel = Channel<NavigationEvent>()
    val navigationFlow: Flow<NavigationEvent> = _navigationChannel.receiveAsFlow()

    fun onAsciiCodeChange(newCode: String) {
        _uiState.update { it.copy(asciiCode = newCode) }
    }

    fun onAddAchievementManually() {
        viewModelScope.launch {
            _navigationChannel.send(NavigationEvent.NavigateToCreatePack)
        }
    }

    fun onCodeSubmit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentCode = _uiState.value.asciiCode
            try {
                val newPack = achievementPackRepository.getAchievementPackByCode(currentCode)
                _uiState.update {
                    it.copy(
                        asciiCode = "",
                        isLoading = false,
                    )
                }
                _messageChannel.send("${newPack.name} pack successfully added")
            } catch (e: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                _messageChannel.send(
                    e.message
                        ?: "Achievement pack with code \"$currentCode\" has already been added"
                )
            } catch (e: NoSuchElementException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                _messageChannel.send(
                    "No achievement pack with code \"$currentCode\" exists"
                )
            }
        }
    }
}