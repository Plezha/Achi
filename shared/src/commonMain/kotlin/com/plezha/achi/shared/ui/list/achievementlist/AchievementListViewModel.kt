package com.plezha.achi.shared.ui.list.achievementlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.model.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementListViewModel(
    private val achievementRepository: AchievementRepository,
    private val achievementPackRepository: AchievementPackRepository
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<Achievement>>(listOf())
    val achievements: StateFlow<List<Achievement>> = _achievements

    fun loadAchievementsByPackId(id: String) {
        viewModelScope.launch(Dispatchers.Default) { // TODO IO
            _achievements.value = achievementPackRepository.getAchievementPackById(id).achievementIds.map {
                achievementRepository.getAchievement(it)
            }
        }
    }

}
