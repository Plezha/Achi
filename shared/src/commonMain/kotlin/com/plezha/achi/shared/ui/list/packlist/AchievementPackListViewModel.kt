package com.plezha.achi.shared.ui.list.packlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.model.overallProgress
import com.plezha.achi.shared.data.toStepProgressList
import com.plezha.achi.shared.ui.list.achievementdetails.withProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackListUiState(
    val packs: List<AchievementPack> = emptyList(),
    val packProgress: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AchievementPackListViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackListUiState())
    val uiState: StateFlow<PackListUiState> = _uiState

    init {
        // Observe auth state to load packs when logged in
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update { it.copy(isLoggedIn = authState.isLoggedIn) }
                if (authState.isLoggedIn) {
                    loadUserPacks()
                } else {
                    // Clear packs when logged out
                    _uiState.update { it.copy(packs = emptyList(), packProgress = emptyMap()) }
                }
            }
        }
        
        // Observe packs from repository and compute progress when packs change
        viewModelScope.launch {
            userRepository.userPacks.collect { packs ->
                _uiState.update { it.copy(packs = packs) }
                if (packs.isNotEmpty() && authRepository.authState.value.isLoggedIn) {
                    loadPacksProgress(packs)
                }
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

    private fun loadPacksProgress(packs: List<AchievementPack>) {
        viewModelScope.launch {
            val progressMap = packs.map { pack ->
                async {
                    try {
                        val achievements = pack.achievementIds.map { id ->
                            achievementRepository.getAchievement(id)
                        }.map { achievement ->
                            try {
                                val progress = userRepository.getProgress(achievement.id)
                                if (progress != null) {
                                    achievement.withProgress(progress.toStepProgressList(), progress.isCompleted)
                                } else {
                                    achievement
                                }
                            } catch (_: Exception) {
                                achievement
                            }
                        }
                        pack.id to achievements.overallProgress()
                    } catch (_: Exception) {
                        pack.id to 0f
                    }
                }
            }.awaitAll().toMap()

            _uiState.update { it.copy(packProgress = progressMap) }
        }
    }
}