package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.auth.AuthRepository
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
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false
)

sealed class NavigationEvent {
    object NavigateToCreatePack : NavigationEvent()
}

class AddAchievementsViewModel(
    private val achievementPackRepository: AchievementPackRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddAchievementUiState())
    val uiState: StateFlow<AddAchievementUiState> = _uiState.asStateFlow()

    private val _messageChannel = Channel<String>()
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

    private val _navigationChannel = Channel<NavigationEvent>()
    val navigationFlow: Flow<NavigationEvent> = _navigationChannel.receiveAsFlow()

    init {
        // Observe auth state
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update { it.copy(isLoggedIn = authState.isLoggedIn) }
            }
        }
    }

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
            // Check if logged in first
            if (!_uiState.value.isLoggedIn) {
                _messageChannel.send("Please log in to add achievement packs")
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }

            val currentCode = _uiState.value.asciiCode
            try {
                // Use UserRepository to add pack to user's collection
                val newPack = userRepository.addPackByCode(currentCode)
                _uiState.update {
                    it.copy(
                        asciiCode = "",
                        isLoading = false,
                    )
                }
                _messageChannel.send("${newPack.name} pack successfully added")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                // Handle different error cases based on response
                val errorMessage = when {
                    e.message?.contains("409") == true -> 
                        "Achievement pack with code \"$currentCode\" is already in your collection"
                    e.message?.contains("404") == true -> 
                        "No achievement pack with code \"$currentCode\" exists"
                    e.message?.contains("401") == true -> 
                        "Please log in to add achievement packs"
                    else -> e.message ?: "Failed to add pack"
                }
                _messageChannel.send(errorMessage)
            }
        }
    }
}