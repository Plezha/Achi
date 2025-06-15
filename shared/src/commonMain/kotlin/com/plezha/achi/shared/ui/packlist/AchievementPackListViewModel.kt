package com.plezha.achi.shared.ui.packlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPack
import com.plezha.achi.shared.data.AchievementPackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementPackListViewModel(
    private val repository: AchievementPackRepository
) : ViewModel() {
    private val _achievementPacks = MutableStateFlow<List<AchievementPack>>(emptyList())
    val achievementPacks: StateFlow<List<AchievementPack>> = _achievementPacks

    init {
        viewModelScope.launch {
            _achievementPacks.value = repository.getAchievementPacks()
        }
    }
}