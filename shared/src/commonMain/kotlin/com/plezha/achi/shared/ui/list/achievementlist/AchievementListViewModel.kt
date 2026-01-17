package com.plezha.achi.shared.ui.list.achievementlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AchievementListUiState(
    val pack: AchievementPack? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

class AchievementListViewModel(
    private val achievementRepository: AchievementRepository,
    private val achievementPackRepository: AchievementPackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementListUiState())
    val uiState: StateFlow<AchievementListUiState> = _uiState.asStateFlow()
    
    // For backward compatibility
    private val _achievements = MutableStateFlow<List<Achievement>>(listOf())
    val achievements: StateFlow<List<Achievement>> = _achievements

    fun loadAchievementsByPackId(id: String) {
        viewModelScope.launch(Dispatchers.Default) { // TODO IO
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val pack = achievementPackRepository.getAchievementPackById(id)
            val achievementsList = pack.achievementIds.map {
                achievementRepository.getAchievement(it)
            }
            
            _achievements.value = achievementsList
            _uiState.value = AchievementListUiState(
                pack = pack,
                achievements = achievementsList,
                isLoading = false
            )
        }
    }

}
