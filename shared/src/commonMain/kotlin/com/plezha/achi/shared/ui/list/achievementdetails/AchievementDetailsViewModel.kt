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
import com.plezha.achi.shared.data.toStepProgressMap
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
                            achievement.withProgress(progress.toStepProgressMap(), progress.isCompleted)
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

    fun setStepCompleted(completedStep: AchievementStep, completed: Boolean) {
        val newSubstepsDone = if (completed) completedStep.progress.substepsAmount else 0
        updateStepProgressInternal(completedStep.id, newSubstepsDone) { achievement, stepId ->
            achievement.copy(
                steps = achievement.steps.map { step ->
                    if (step.id == stepId) {
                        if (completed) step.asCompleted() else step.asNotStarted()
                    } else {
                        step
                    }
                }
            )
        }
    }

    fun increaseStepProgress(updatedStep: AchievementStep) {
        val newSubstepsDone = (updatedStep.progress.substepsDone + 1)
            .coerceAtMost(updatedStep.progress.substepsAmount)
        updateStepProgressInternal(updatedStep.id, newSubstepsDone) { achievement, stepId ->
            achievement.copy(
                steps = achievement.steps.map { step ->
                    if (step.id == stepId) step.withProgressIncreased() else step
                }
            )
        }
    }

    fun decreaseStepProgress(updatedStep: AchievementStep) {
        val newSubstepsDone = (updatedStep.progress.substepsDone - 1).coerceAtLeast(0)
        updateStepProgressInternal(updatedStep.id, newSubstepsDone) { achievement, stepId ->
            achievement.copy(
                steps = achievement.steps.map { step ->
                    if (step.id == stepId) step.withProgressDecreased() else step
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
        stepId: String,
        newSubstepsDone: Int,
        localUpdate: (Achievement, String) -> Achievement
    ) {
        // Update UI immediately (optimistic update)
        _uiState.update {
            try {
                val achievement = it.achievement!!
                it.copy(achievement = localUpdate(achievement, stepId))
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
                        stepId = stepId,
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
 * Returns a copy of the achievement with the given step progress and completion status applied.
 * Progress is matched by step ID for stable mapping.
 */
fun Achievement.withProgress(progressMap: Map<String, StepProgress>, isCompleted: Boolean = false): Achievement {
    return copy(
        isCompleted = isCompleted,
        steps = steps.map { step ->
            val progress = progressMap[step.id]
            if (progress != null) step.copy(progress = progress) else step
        }
    )
}

data class AchievementDetailsUiState(
    val loading: Boolean = false,
    val errorMessage: UiText? = null,
    val achievement: Achievement? = null
)
