package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditableAchievementData(
    val id: AchievementId,
    val title: String = "",
    val shortDescription: String = "",
    val steps: MutableList<String> = mutableListOf()
)

data class CreateAchievementPackUiState(
    val packName: String = "",
    val packDescription: String = "",
    val achievements: List<EditableAchievementData> = listOf()
)

class CreateAchievementPackViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAchievementPackUiState())
    val uiState: StateFlow<CreateAchievementPackUiState> = _uiState.asStateFlow()

    fun onPackNameChange(name: String) {
        _uiState.update { it.copy(packName = name) }
    }

    fun onPackDescriptionChange(description: String) {
        _uiState.update { it.copy(packDescription = description) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddAchievement() {
        _uiState.update {
            it.copy(
                achievements = it.achievements + EditableAchievementData(
                    id = AchievementId(
                        Uuid.random().toString()
                    )
                )
            )
        }
    }

    fun onAchievementTitleChanged(id: AchievementId, title: String) {
        _uiState.update { it ->
            val updatedAchievements = it.achievements.toMutableList()
            val index = updatedAchievements.indexOfLast { it.id == id }
            updatedAchievements[index] = updatedAchievements[index].copy(title = title)
            it.copy(achievements = updatedAchievements)
        }
    }

    fun onAchievementDescriptionChanged(id: AchievementId, description: String) {
        _uiState.update {
            val updatedAchievements = it.achievements.toMutableList()
            val index = updatedAchievements.indexOfLast { it.id == id }
            updatedAchievements[index] = updatedAchievements[index].copy(shortDescription = description)
            it.copy(achievements = updatedAchievements)
        }
    }

    fun onSaveAchievementPack() {
        // TODO
    }
}

value class AchievementId(val value: String)
