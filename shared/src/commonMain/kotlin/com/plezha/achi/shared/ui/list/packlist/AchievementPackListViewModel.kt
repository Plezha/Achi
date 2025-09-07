package com.plezha.achi.shared.ui.list.packlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.model.AchievementPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AchievementPackListViewModel(
    private val repository: AchievementPackRepository
) : ViewModel() {
    private val _achievementPacks = MutableStateFlow<List<AchievementPack>>(emptyList())
    val achievementPacks: StateFlow<List<AchievementPack>> = _achievementPacks

    init {
        viewModelScope.launch {
            repository.achievementPacks.collect { newPacks ->
                 _achievementPacks.update { newPacks }
            }
        }
    }
}