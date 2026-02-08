package com.plezha.achi.shared.ui.list.achievementlist

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*
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
import com.plezha.achi.shared.data.toStepProgressMap
import com.plezha.achi.shared.ui.common.UiText
import com.plezha.achi.shared.ui.list.achievementdetails.withProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class AchievementListNavigationEvent {
    data class NavigateToCopy(val packId: String) : AchievementListNavigationEvent()
}

data class AchievementListUiState(
    val pack: AchievementPack? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val isOwner: Boolean = false,
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

    private val _messageChannel = Channel<UiText>()
    val messageFlow: Flow<UiText> = _messageChannel.receiveAsFlow()

    private val _navigationChannel = Channel<AchievementListNavigationEvent>()
    val navigationFlow: Flow<AchievementListNavigationEvent> = _navigationChannel.receiveAsFlow()
    
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
                                achievement.withProgress(progress.toStepProgressMap(), progress.isCompleted)
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
                
                val currentUserId = authRepository.authState.value.userId
                val isOwner = currentUserId != null && pack.creatorId == currentUserId

                _achievements.value = achievementsWithProgress
                _uiState.value = AchievementListUiState(
                    pack = pack,
                    achievements = achievementsWithProgress,
                    isLoading = false,
                    isOwner = isOwner
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load achievements"
                )
            }
        }
    }

    fun copyPack() {
        val state = _uiState.value
        val pack = state.pack ?: return
        val achievements = state.achievements
        if (achievements.isEmpty()) return

        if (!authRepository.authState.value.isLoggedIn) {
            viewModelScope.launch {
                _messageChannel.send(UiText.Resource(Res.string.msg_login_required_to_copy))
            }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val copiedPack = achievementPackRepository.copyPack(pack, achievements)
                userRepository.addPackByCode(copiedPack.code)
                _messageChannel.send(UiText.Resource(Res.string.msg_pack_copied, pack.name))
                _navigationChannel.send(AchievementListNavigationEvent.NavigateToCopy(copiedPack.id))
            } catch (e: Exception) {
                _messageChannel.send(
                    if (e.message != null) UiText.Raw(e.message!!)
                    else UiText.Resource(Res.string.error_failed_to_copy_pack)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun showNotOwnerMessage() {
        viewModelScope.launch {
            _messageChannel.send(UiText.Resource(Res.string.msg_only_creator_can_modify))
        }
    }

    fun deletePack(onDeleted: () -> Unit) {
        val pack = _uiState.value.pack ?: return
        viewModelScope.launch {
            try {
                achievementPackRepository.deleteAchievementPack(pack.id)
                try {
                    userRepository.removePackFromCollection(pack.id)
                } catch (_: Exception) { }
                _messageChannel.send(UiText.Resource(Res.string.msg_pack_deleted, pack.name))
                onDeleted()
            } catch (e: Exception) {
                _messageChannel.send(
                    if (e.message != null) UiText.Raw(e.message!!)
                    else UiText.Resource(Res.string.error_failed_to_delete_pack)
                )
            }
        }
    }
}
