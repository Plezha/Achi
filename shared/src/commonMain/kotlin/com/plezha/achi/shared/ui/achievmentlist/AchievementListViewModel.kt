package com.plezha.achi.shared.ui.achievmentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plezha.achi.shared.data.Achievement
import com.plezha.achi.shared.data.AchievementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementListViewModel(
    private val repository: AchievementRepository
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements

    init {
        viewModelScope.launch {
            _achievements.value = repository.getAchievements("")
        }
    }

}