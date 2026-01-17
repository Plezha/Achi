package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.network.models.AchievementCreateBody
import com.plezha.achi.shared.data.network.models.AchievementStepCreate
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditableStep(
    val description: String = "",
    val substepsAmount: Int = 1
)

data class EditableAchievementData(
    val id: AchievementId,
    val title: String = "",
    val shortDescription: String = "",
    val longDescription: String = "",
    val steps: List<EditableStep> = listOf(),
    val imageFile: PlatformFile? = null
)

data class CreateAchievementPackUiState(
    val packName: String = "",
    val packDescription: String = "",
    val achievements: List<EditableAchievementData> = listOf(),
    val selectedImageFile: PlatformFile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = packName.isNotBlank() && 
                achievements.isNotEmpty() && 
                achievements.all { it.title.isNotBlank() } &&
                selectedImageFile != null &&
                !isLoading
}

sealed class CreatePackNavigationEvent {
    data object NavigateBack : CreatePackNavigationEvent()
}

class CreateAchievementPackViewModel(
    private val repository: AchievementPackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAchievementPackUiState())
    val uiState: StateFlow<CreateAchievementPackUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<CreatePackNavigationEvent>()
    val navigationFlow: Flow<CreatePackNavigationEvent> = _navigationChannel.receiveAsFlow()

    private val _messageChannel = Channel<String>()
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

    fun onPackNameChange(name: String) {
        _uiState.update { it.copy(packName = name, errorMessage = null) }
    }

    fun onPackDescriptionChange(description: String) {
        _uiState.update { it.copy(packDescription = description, errorMessage = null) }
    }

    fun onImageSelected(file: PlatformFile?) {
        _uiState.update { it.copy(selectedImageFile = file, errorMessage = null) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddAchievement() {
        _uiState.update {
            it.copy(
                achievements = it.achievements + EditableAchievementData(
                    id = AchievementId(Uuid.random().toString())
                ),
                errorMessage = null
            )
        }
    }

    fun onRemoveAchievement(id: AchievementId) {
        _uiState.update {
            it.copy(
                achievements = it.achievements.filter { achievement -> achievement.id != id },
                errorMessage = null
            )
        }
    }

    fun onAchievementTitleChanged(id: AchievementId, title: String) {
        _uiState.update { state ->
            val updatedAchievements = state.achievements.map { achievement ->
                if (achievement.id == id) achievement.copy(title = title) else achievement
            }
            state.copy(achievements = updatedAchievements, errorMessage = null)
        }
    }

    fun onAchievementDescriptionChanged(id: AchievementId, description: String) {
        _uiState.update { state ->
            val updatedAchievements = state.achievements.map { achievement ->
                if (achievement.id == id) achievement.copy(shortDescription = description) else achievement
            }
            state.copy(achievements = updatedAchievements, errorMessage = null)
        }
    }

    fun updateAchievementAtIndex(index: Int, achievement: EditableAchievementData) {
        _uiState.update { state ->
            val updatedAchievements = state.achievements.toMutableList()
            if (index in updatedAchievements.indices) {
                updatedAchievements[index] = achievement
            }
            state.copy(achievements = updatedAchievements, errorMessage = null)
        }
    }

    fun getAchievementAtIndex(index: Int): EditableAchievementData? {
        return _uiState.value.achievements.getOrNull(index)
    }

    fun onSaveAchievementPack() {
        val currentState = _uiState.value

        // Validation
        if (currentState.packName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pack name is required") }
            return
        }
        if (currentState.achievements.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one achievement") }
            return
        }
        if (currentState.achievements.any { it.title.isBlank() }) {
            _uiState.update { it.copy(errorMessage = "All achievements must have a title") }
            return
        }
        val imageFile = currentState.selectedImageFile
        if (imageFile == null) {
            _uiState.update { it.copy(errorMessage = "Please select a preview image") }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Convert EditableAchievementData to AchievementCreateBody
                val achievementBodies = currentState.achievements.map { achievement ->
                    AchievementCreateBody(
                        title = achievement.title,
                        shortDescription = achievement.shortDescription.ifBlank { achievement.title },
                        steps = if (achievement.steps.isEmpty()) {
                            // Default step if none provided
                            listOf(AchievementStepCreate(description = "Complete ${achievement.title}"))
                        } else {
                            achievement.steps.map { step ->
                                AchievementStepCreate(
                                    description = step.description,
                                    substepsAmount = step.substepsAmount
                                )
                            }
                        },
                        longDescription = achievement.longDescription.ifBlank { null }
                        // imageUrl and previewImageUrl will be set by repository after uploading
                    )
                }

                // Read file bytes and name from PlatformFile
                // Using readBytes() as per FileKit docs: https://filekit.mintlify.app/core/platform-file
                val imageBytes = imageFile.readBytes()
                val fileName = imageFile.name

                // Collect achievement images (index -> (bytes, filename))
                val achievementImages = mutableMapOf<Int, Pair<ByteArray, String>>()
                currentState.achievements.forEachIndexed { index, achievement ->
                    achievement.imageFile?.let { file ->
                        achievementImages[index] = Pair(file.readBytes(), file.name)
                    }
                }

                // Create the pack
                val createdPack = repository.createAchievementPack(
                    name = currentState.packName,
                    achievements = achievementBodies,
                    imageBytes = imageBytes,
                    imageFileName = fileName,
                    achievementImages = achievementImages
                )
                
                // Add the created pack to user's collection
                try {
                    userRepository.addPackByCode(createdPack.code)
                } catch (e: Exception) {
                    // Pack created but failed to add to collection - log but don't fail
                    println("Warning: Pack created but failed to add to collection: ${e.message}")
                }

                // Reset the form after successful creation
                _uiState.value = CreateAchievementPackUiState()
                _messageChannel.send("Pack \"${currentState.packName}\" created successfully! Code: ${createdPack.code}")
                _navigationChannel.send(CreatePackNavigationEvent.NavigateBack)

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Failed to create pack"
                    ) 
                }
            }
        }
    }
}

@JvmInline
value class AchievementId(val value: String)
