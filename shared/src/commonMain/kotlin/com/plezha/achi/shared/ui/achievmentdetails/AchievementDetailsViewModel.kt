package com.plezha.achi.shared.ui.achievmentdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.Achievement
import com.plezha.achi.shared.data.AchievementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AchievementDetailsViewModel(
    private val repository: AchievementRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementDetailsUiState(loading = true))
    val uiState: StateFlow<AchievementDetailsUiState> = _uiState

    fun loadAchievementById(id: String) {
        viewModelScope.launch(Dispatchers.Default) { // TODO IO
            _uiState.value = AchievementDetailsUiState(achievement = repository.getAchievementById(id))
        }
    }

    fun incrementAchievementProgress() {
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(
                    achievement = achievement.copy(
                        stepsDone = achievement.stepsDone + 1
                    )
                )
            } catch (e: Exception) {
                it.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun incrementStepProgress() {
        val achievement = _uiState.value.achievement!!
        val currentStep = achievement.steps[achievement.stepsDone]
        _uiState.update {
            try {
                currentStep.progress!!
                it.copy(
                    achievement = achievement.copy(
                        steps = achievement.steps.toMutableList().apply {
                            set(
                                achievement.stepsDone, currentStep.copy(
                                    progress = currentStep.progress.copy(
                                        stepsDone = currentStep.progress.stepsDone + 1
                                    )
                                )
                            )
                        }
                    )
                )
            } catch (e: Exception) {
                it.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
        if (currentStep.progress?.totalSteps?.dec() == currentStep.progress?.stepsDone) {
            incrementAchievementProgress()
        }
    }

}

data class AchievementDetailsUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val achievement: Achievement? = null
)