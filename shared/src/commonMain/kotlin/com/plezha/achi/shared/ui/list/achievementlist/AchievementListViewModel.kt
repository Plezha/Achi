package com.plezha.achi.shared.ui.list.achievementlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.model.completedCount
import com.plezha.achi.shared.data.model.overallProgress
import com.plezha.achi.shared.data.toStepProgressList
import com.plezha.achi.shared.ui.list.achievementdetails.withProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AchievementListUiState(
    val pack: AchievementPack? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Overall progress across all achievements (0.0 to 1.0) */
    val overallProgress: Float
        get() = achievements.overallProgress()
    
    /** Number of fully completed achievements */
    val completedCount: Int
        get() = achievements.completedCount()
}

class AchievementListViewModel(
    private val achievementRepository: AchievementRepository,
    private val achievementPackRepository: AchievementPackRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementListUiState())
    val uiState: StateFlow<AchievementListUiState> = _uiState.asStateFlow()
    
    // For backward compatibility
    private val _achievements = MutableStateFlow<List<Achievement>>(listOf())
    val achievements: StateFlow<List<Achievement>> = _achievements

    fun loadAchievementsByPackId(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val pack = achievementPackRepository.getAchievementPackById(id)
                val achievementsList = pack.achievementIds.map {
                    achievementRepository.getAchievement(it)
                }
                
                // Merge with user progress if logged in
                val isLoggedIn = authRepository.authState.value.isLoggedIn
                val achievementsWithProgress = if (isLoggedIn) {
                    achievementsList.map { achievement ->
                        try {
                            val progress = userRepository.getProgress(achievement.id)
                            if (progress != null) {
                                achievement.withProgress(progress.toStepProgressList())
                            } else {
                                achievement
                            }
                        } catch (e: Exception) {
                            achievement
                        }
                    }
                } else {
                    achievementsList
                }
                
                _achievements.value = achievementsWithProgress
                _uiState.value = AchievementListUiState(
                    pack = pack,
                    achievements = achievementsWithProgress,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load achievements"
                )
            }
        }
    }

}
