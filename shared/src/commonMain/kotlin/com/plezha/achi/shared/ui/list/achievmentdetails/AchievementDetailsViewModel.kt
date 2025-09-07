package com.plezha.achi.shared.ui.list.achievmentdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementStep
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
            _uiState.value = AchievementDetailsUiState(achievement = repository.getAchievement(id))
        }
    }

    fun setStepCompleted(completedStep: AchievementStep, completed: Boolean) {
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(
                    achievement = achievement.copy(
                        steps = achievement.steps.map { step ->
                            if (completedStep == step) {
                                if (completed) step.asCompleted() else step.asNotStarted()
                            } else {
                                step
                            }
                        }
                    )
                )
            } catch (e: Exception) {
                it.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun increaseStepProgress(updatedStep: AchievementStep) {
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(
                    achievement = achievement.copy(
                        steps = achievement.steps.map { step ->
                            if (updatedStep == step) step.withProgressIncreased() else step
                        }
                    )
                )
            } catch (e: Exception) {
                it.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun decreaseStepProgress(updatedStep: AchievementStep) {
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(
                    achievement = achievement.copy(
                        steps = achievement.steps.map { step ->
                            if (updatedStep == step) step.withProgressDecreased() else step
                        }
                    )
                )
            } catch (e: Exception) {
                it.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

}

fun AchievementStep.withProgressIncreased() =
    copy(progress = progress.copy(substepsDone = progress.substepsDone + 1))

fun AchievementStep.withProgressDecreased() =
    copy(progress = progress.copy(substepsDone = progress.substepsDone - 1))

data class AchievementDetailsUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val achievement: Achievement? = null
)