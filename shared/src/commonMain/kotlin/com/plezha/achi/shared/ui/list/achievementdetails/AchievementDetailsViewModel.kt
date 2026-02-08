package com.plezha.achi.shared.ui.list.achievementdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementStep
import com.plezha.achi.shared.data.model.StepProgress
import com.plezha.achi.shared.data.toStepProgressList
import com.plezha.achi.shared.ui.common.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AchievementDetailsViewModel(
    private val repository: AchievementRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementDetailsUiState(loading = true))
    val uiState: StateFlow<AchievementDetailsUiState> = _uiState
    
    private var currentAchievementId: String? = null

    fun loadAchievementById(id: String) {
        currentAchievementId = id
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = AchievementDetailsUiState(loading = true)
            try {
                // Load achievement data
                val achievement = repository.getAchievement(id)
                
                // Try to load user's progress if logged in
                val isLoggedIn = authRepository.authState.value.isLoggedIn
                val achievementWithProgress = if (isLoggedIn) {
                    try {
                        val progress = userRepository.getProgress(id)
                        if (progress != null) {
                            // Merge server progress with achievement steps
                            achievement.withProgress(progress.toStepProgressList(), progress.isCompleted)
                        } else {
                            achievement
                        }
                    } catch (e: Exception) {
                        // Failed to load progress, use default
                        achievement
                    }
                } else {
                    achievement
                }
                
                _uiState.value = AchievementDetailsUiState(achievement = achievementWithProgress)
            } catch (e: Exception) {
                _uiState.value = AchievementDetailsUiState(
                    errorMessage = if (e.message != null) UiText.Raw(e.message!!) else UiText.Resource(Res.string.error_failed_to_load_achievement)
                )
            }
        }
    }

    fun setStepCompleted(completedStep: AchievementStep, completed: Boolean, stepIndex: Int) {
        val newSubstepsDone = if (completed) completedStep.progress.substepsAmount else 0
        updateStepProgressInternal(stepIndex, newSubstepsDone) { achievement, idx ->
            achievement.copy(
                steps = achievement.steps.mapIndexed { i, step ->
                    if (i == idx) {
                        if (completed) step.asCompleted() else step.asNotStarted()
                    } else {
                        step
                    }
                }
            )
        }
    }

    fun increaseStepProgress(updatedStep: AchievementStep, stepIndex: Int) {
        val newSubstepsDone = (updatedStep.progress.substepsDone + 1)
            .coerceAtMost(updatedStep.progress.substepsAmount)
        updateStepProgressInternal(stepIndex, newSubstepsDone) { achievement, idx ->
            achievement.copy(
                steps = achievement.steps.mapIndexed { i, step ->
                    if (i == idx) step.withProgressIncreased() else step
                }
            )
        }
    }

    fun decreaseStepProgress(updatedStep: AchievementStep, stepIndex: Int) {
        val newSubstepsDone = (updatedStep.progress.substepsDone - 1).coerceAtLeast(0)
        updateStepProgressInternal(stepIndex, newSubstepsDone) { achievement, idx ->
            achievement.copy(
                steps = achievement.steps.mapIndexed { i, step ->
                    if (i == idx) step.withProgressDecreased() else step
                }
            )
        }
    }

    fun toggleCompletion() {
        val achievement = _uiState.value.achievement ?: return
        val newIsCompleted = !achievement.isCompleted

        // Optimistic UI update
        _uiState.update {
            it.copy(achievement = achievement.copy(isCompleted = newIsCompleted))
        }

        // Sync to server if logged in
        val achievementId = currentAchievementId ?: return
        val isLoggedIn = authRepository.authState.value.isLoggedIn

        if (isLoggedIn) {
            viewModelScope.launch {
                try {
                    userRepository.updateAchievementCompletion(
                        achievementId = achievementId,
                        isCompleted = newIsCompleted
                    )
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(errorMessage = UiText.Resource(Res.string.error_failed_to_sync_progress, e.message ?: ""))
                    }
                }
            }
        }
    }
    
    private fun updateStepProgressInternal(
        stepIndex: Int,
        newSubstepsDone: Int,
        localUpdate: (Achievement, Int) -> Achievement
    ) {
        // Update UI immediately (optimistic update)
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(achievement = localUpdate(achievement, stepIndex))
            } catch (e: Exception) {
                it.copy(errorMessage = if (e.message != null) UiText.Raw(e.message!!) else UiText.Resource(Res.string.error_unknown))
            }
        }
        
        // Sync to server if logged in
        val achievementId = currentAchievementId ?: return
        val isLoggedIn = authRepository.authState.value.isLoggedIn
        
        if (isLoggedIn) {
            viewModelScope.launch {
                try {
                    userRepository.updateStepProgress(
                        achievementId = achievementId,
                        stepIndex = stepIndex,
                        substepsDone = newSubstepsDone
                    )
                } catch (e: Exception) {
                    // Log error but don't revert UI - could show a sync indicator
                    _uiState.update { 
                        it.copy(errorMessage = UiText.Resource(Res.string.error_failed_to_sync_progress, e.message ?: ""))
                    }
                }
            }
        }
    }
}

fun AchievementStep.withProgressIncreased() =
    copy(progress = progress.copy(substepsDone = (progress.substepsDone + 1).coerceAtMost(progress.substepsAmount)))

fun AchievementStep.withProgressDecreased() =
    copy(progress = progress.copy(substepsDone = (progress.substepsDone - 1).coerceAtLeast(0)))

/**
 * Returns a copy of the achievement with the given step progress and completion status applied
 */
fun Achievement.withProgress(progressList: List<StepProgress>, isCompleted: Boolean = false): Achievement {
    return copy(
        isCompleted = isCompleted,
        steps = if (progressList.size != steps.size) steps else {
            steps.mapIndexed { index, step ->
                step.copy(progress = progressList[index])
            }
        }
    )
}

data class AchievementDetailsUiState(
    val loading: Boolean = false,
    val errorMessage: UiText? = null,
    val achievement: Achievement? = null
)
