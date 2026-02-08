package com.plezha.achi.shared.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.network.models.AchievementCreateBody
import com.plezha.achi.shared.data.network.models.AchievementStepCreate
import com.plezha.achi.shared.ui.common.UiText
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
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*

data class EditableStep(
    val description: String = "",
    val substepsAmount: Int = 1
)

data class EditableAchievementData(
    val id: AchievementId,
    val serverId: String? = null,
    val title: String = "",
    val shortDescription: String = "",
    val longDescription: String = "",
    val steps: List<EditableStep> = listOf(),
    val imageFile: PlatformFile? = null,
    val existingImageUrl: String? = null
)

data class CreateAchievementPackUiState(
    val packId: String? = null,
    val packName: String = "",
    val packDescription: String = "",
    val achievements: List<EditableAchievementData> = listOf(),
    val selectedImageFile: PlatformFile? = null,
    val existingPreviewImageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val isEditMode: Boolean get() = packId != null

    val hasImage: Boolean
        get() = selectedImageFile != null || existingPreviewImageUrl != null

    val canSave: Boolean
        get() = packName.isNotBlank() && 
                achievements.isNotEmpty() && 
                achievements.all { it.title.isNotBlank() } &&
                hasImage &&
                !isLoading
}

sealed class CreatePackNavigationEvent {
    data object NavigateBack : CreatePackNavigationEvent()
}

class CreateAchievementPackViewModel(
    private val repository: AchievementPackRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository? = null,
    private val editPackId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAchievementPackUiState(packId = editPackId))
    val uiState: StateFlow<CreateAchievementPackUiState> = _uiState.asStateFlow()

    private val _navigationChannel = Channel<CreatePackNavigationEvent>()
    val navigationFlow: Flow<CreatePackNavigationEvent> = _navigationChannel.receiveAsFlow()

    private val _messageChannel = Channel<UiText>()
    val messageFlow: Flow<UiText> = _messageChannel.receiveAsFlow()

    /** Tracks original achievement IDs when editing, to detect removed achievements */
    private var originalAchievementServerIds: List<String> = emptyList()

    fun onPackNameChange(name: String) {
        _uiState.update { it.copy(packName = name, errorMessage = null) }
    }

    fun onPackDescriptionChange(description: String) {
        _uiState.update { it.copy(packDescription = description, errorMessage = null) }
    }

    fun onImageSelected(file: PlatformFile?) {
        _uiState.update { it.copy(selectedImageFile = file, errorMessage = null) }
    }

    /**
     * Loads an existing pack and its achievements into the form for editing.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun loadExistingPack(packId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val pack = repository.getAchievementPackById(packId)
                val achievements = pack.achievementIds.map { id ->
                    val achievement = achievementRepository!!.getAchievement(id)
                    EditableAchievementData(
                        id = AchievementId(Uuid.random().toString()),
                        serverId = achievement.id,
                        title = achievement.title,
                        shortDescription = achievement.shortDescription,
                        longDescription = achievement.longDescription ?: "",
                        steps = achievement.steps.map { step ->
                            EditableStep(
                                description = step.description,
                                substepsAmount = step.progress.substepsAmount
                            )
                        },
                        existingImageUrl = achievement.imageUrl
                    )
                }
                originalAchievementServerIds = pack.achievementIds
                _uiState.value = CreateAchievementPackUiState(
                    packId = packId,
                    packName = pack.name,
                    packDescription = "", // description not in domain model yet
                    achievements = achievements,
                    existingPreviewImageUrl = pack.previewImageUrl,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (e.message != null) UiText.Raw(e.message!!) else UiText.Resource(Res.string.error_failed_to_load_packs)
                    )
                }
            }
        }
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
            _uiState.update { it.copy(errorMessage = UiText.Resource(Res.string.error_pack_name_required)) }
            return
        }
        if (currentState.achievements.isEmpty()) {
            _uiState.update { it.copy(errorMessage = UiText.Resource(Res.string.error_add_achievement)) }
            return
        }
        if (currentState.achievements.any { it.title.isBlank() }) {
            _uiState.update { it.copy(errorMessage = UiText.Resource(Res.string.error_achievement_title_required)) }
            return
        }
        if (!currentState.hasImage) {
            _uiState.update { it.copy(errorMessage = UiText.Resource(Res.string.error_select_preview_image)) }
            return
        }

        if (currentState.isEditMode) {
            saveEditedPack(currentState)
        } else {
            saveNewPack(currentState)
        }
    }

    private fun saveNewPack(currentState: CreateAchievementPackUiState) {
        val imageFile = currentState.selectedImageFile ?: return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Convert EditableAchievementData to AchievementCreateBody
                val achievementBodies = currentState.achievements.map { achievement ->
                    AchievementCreateBody(
                        title = achievement.title,
                        shortDescription = achievement.shortDescription.ifBlank { achievement.title },
                        steps = achievement.steps.map { step ->
                            AchievementStepCreate(
                                description = step.description,
                                substepsAmount = step.substepsAmount
                            )
                        },
                        longDescription = achievement.longDescription.ifBlank { null }
                    )
                }

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
                    println("Warning: Pack created but failed to add to collection: ${e.message}")
                }

                _uiState.value = CreateAchievementPackUiState()
                _messageChannel.send(UiText.Resource(Res.string.msg_pack_created, currentState.packName, createdPack.code))
                _navigationChannel.send(CreatePackNavigationEvent.NavigateBack)

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = if (e.message != null) UiText.Raw(e.message!!) else UiText.Resource(Res.string.error_failed_to_create_pack)
                    ) 
                }
            }
        }
    }

    private fun saveEditedPack(currentState: CreateAchievementPackUiState) {
        val packId = currentState.packId ?: return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Convert to repository data model
                val achievementEditData = currentState.achievements.map { achievement ->
                    com.plezha.achi.shared.data.AchievementEditData(
                        serverId = achievement.serverId,
                        title = achievement.title,
                        shortDescription = achievement.shortDescription.ifBlank { achievement.title },
                        longDescription = achievement.longDescription.ifBlank { null },
                        steps = achievement.steps.map { step ->
                            com.plezha.achi.shared.data.AchievementStepEditData(
                                description = step.description,
                                substepsAmount = step.substepsAmount
                            )
                        }
                    )
                }

                // Collect achievement images
                val achievementImages = mutableMapOf<Int, Pair<ByteArray, String>>()
                currentState.achievements.forEachIndexed { index, achievement ->
                    achievement.imageFile?.let { file ->
                        achievementImages[index] = Pair(file.readBytes(), file.name)
                    }
                }

                // Upload new pack preview image if changed
                val newImageBytes = currentState.selectedImageFile?.readBytes()
                val newImageFileName = currentState.selectedImageFile?.name

                val updatedPack = repository.updateAchievementPack(
                    packId = packId,
                    name = currentState.packName,
                    achievements = achievementEditData,
                    originalAchievementIds = originalAchievementServerIds,
                    imageBytes = newImageBytes,
                    imageFileName = newImageFileName,
                    achievementImages = achievementImages
                )

                // Invalidate caches so AchievementListScreen fetches fresh data
                repository.invalidatePackCache(packId)
                val allAffectedIds = (originalAchievementServerIds + updatedPack.achievementIds).toSet()
                achievementRepository?.invalidateCache(allAffectedIds)

                _messageChannel.send(UiText.Resource(Res.string.msg_pack_updated, currentState.packName))
                _navigationChannel.send(CreatePackNavigationEvent.NavigateBack)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (e.message != null) UiText.Raw(e.message!!) else UiText.Resource(Res.string.error_failed_to_update_pack)
                    )
                }
            }
        }
    }
}

@JvmInline
value class AchievementId(val value: String)
