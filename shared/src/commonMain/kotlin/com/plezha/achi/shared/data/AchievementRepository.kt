package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.check
import com.plezha.achi.shared.data.network.toAchievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface AchievementRepository {
    val achievements: Flow<List<Achievement>>

    suspend fun getAchievement(id: String): Achievement
    suspend fun createAchievement(achievement: Achievement)
    suspend fun editAchievement(achievement: Achievement)
    suspend fun deleteAchievementById(id: String)
}

class AchievementRepositoryImpl(
    private val achievementsApi: AchievementsApi,
) : AchievementRepository {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    override val achievements = _achievements.asStateFlow()

    override suspend fun getAchievement(id: String): Achievement {
        val ramAchievement = _achievements.value.find { it.id == id }
        if (ramAchievement != null) {
            return ramAchievement
        }
        val response = achievementsApi.getAchievementAchievementsAchievementIdGet(id)
        response.check()
        val achievement = response.body().toAchievement()
        _achievements.update { it + achievement }
        return achievement
    }

    override suspend fun createAchievement(achievement: Achievement) {
        TODO("Not yet implemented")
    }

    override suspend fun editAchievement(achievement: Achievement) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAchievementById(id: String) {
        TODO("Not yet implemented")
    }
}
