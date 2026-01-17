package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

data class EditAchievementUiState(
    val title: String = "",
    val shortDescription: String = "",
    val longDescription: String = "",
    val steps: List<EditableStep> = listOf(),
    val imageFile: PlatformFile? = null
) {
    val canSave: Boolean
        get() = title.isNotBlank()
}

sealed class EditAchievementNavigationEvent {
    data class SaveAndNavigateBack(val achievement: EditableAchievementData) : EditAchievementNavigationEvent()
    data object Cancel : EditAchievementNavigationEvent()
}

class EditAchievementViewModel(
    private val achievementId: AchievementId,
    initialData: EditableAchievementData? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        initialData?.let { data ->
            EditAchievementUiState(
                title = data.title,
                shortDescription = data.shortDescription,
                longDescription = data.longDescription,
                steps = data.steps,
                imageFile = data.imageFile
            )
        } ?: EditAchievementUiState()
    )
    val uiState: StateFlow<EditAchievementUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<EditAchievementNavigationEvent>()
    val navigationFlow: Flow<EditAchievementNavigationEvent> = _navigationChannel.receiveAsFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onShortDescriptionChanged(description: String) {
        _uiState.update { it.copy(shortDescription = description) }
    }

    fun onLongDescriptionChanged(description: String) {
        _uiState.update { it.copy(longDescription = description) }
    }

    fun onImageSelected(file: PlatformFile?) {
        _uiState.update { it.copy(imageFile = file) }
    }

    fun onAddStep() {
        _uiState.update { state ->
            state.copy(steps = state.steps + EditableStep())
        }
    }

    fun onRemoveStep(index: Int) {
        _uiState.update { state ->
            val updatedSteps = state.steps.toMutableList()
            if (index in updatedSteps.indices) {
                updatedSteps.removeAt(index)
            }
            state.copy(steps = updatedSteps)
        }
    }

    fun onStepDescriptionChanged(index: Int, description: String) {
        _uiState.update { state ->
            val updatedSteps = state.steps.toMutableList()
            if (index in updatedSteps.indices) {
                updatedSteps[index] = updatedSteps[index].copy(description = description)
            }
            state.copy(steps = updatedSteps)
        }
    }

    fun onStepSubstepsAmountChanged(index: Int, amount: Int) {
        _uiState.update { state ->
            val updatedSteps = state.steps.toMutableList()
            if (index in updatedSteps.indices) {
                updatedSteps[index] = updatedSteps[index].copy(substepsAmount = amount.coerceIn(1, 100))
            }
            state.copy(steps = updatedSteps)
        }
    }

    suspend fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        val achievement = EditableAchievementData(
            id = achievementId,
            title = state.title,
            shortDescription = state.shortDescription,
            longDescription = state.longDescription,
            steps = state.steps,
            imageFile = state.imageFile
        )
        _navigationChannel.send(EditAchievementNavigationEvent.SaveAndNavigateBack(achievement))
    }

    suspend fun onCancel() {
        _navigationChannel.send(EditAchievementNavigationEvent.Cancel)
    }
}
