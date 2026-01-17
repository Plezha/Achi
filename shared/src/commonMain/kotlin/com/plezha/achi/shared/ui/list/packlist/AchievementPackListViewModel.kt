package com.plezha.achi.shared.ui.list.packlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.model.AchievementPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackListUiState(
    val packs: List<AchievementPack> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AchievementPackListViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackListUiState())
    val uiState: StateFlow<PackListUiState> = _uiState

    // For backward compatibility
    val achievementPacks: StateFlow<List<AchievementPack>> 
        get() = MutableStateFlow(_uiState.value.packs)

    init {
        // Observe auth state to load packs when logged in
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update { it.copy(isLoggedIn = authState.isLoggedIn) }
                if (authState.isLoggedIn) {
                    loadUserPacks()
                } else {
                    // Clear packs when logged out
                    _uiState.update { it.copy(packs = emptyList()) }
                }
            }
        }
        
        // Observe packs from repository
        viewModelScope.launch {
            userRepository.userPacks.collect { packs ->
                _uiState.update { it.copy(packs = packs) }
            }
        }
    }
    
    fun loadUserPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                userRepository.loadUserPacks()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load packs"
                    ) 
                }
            }
        }
    }
    
    fun refresh() {
        if (_uiState.value.isLoggedIn) {
            loadUserPacks()
        }
    }
}