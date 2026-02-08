package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.check
import com.plezha.achi.shared.data.network.models.AchievementUpdateBody
import com.plezha.achi.shared.data.network.models.AchievementStepCreate
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

    /** Removes cached achievements so the next [getAchievement] call fetches fresh data from the server. */
    fun invalidateCache(ids: Collection<String>)
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
        val response = achievementsApi.updateAchievementAchievementsAchievementIdPut(
            achievementId = achievement.id,
            achievementUpdateBody = AchievementUpdateBody(
                title = achievement.title,
                shortDescription = achievement.shortDescription,
                longDescription = achievement.longDescription,
                previewImageUrl = achievement.previewImageUrl,
                imageUrl = achievement.imageUrl,
                steps = achievement.steps.map { step ->
                    AchievementStepCreate(
                        id = step.id,
                        description = step.description,
                        substepsAmount = step.progress.substepsAmount
                    )
                }
            )
        )
        response.check()
        val updatedAchievement = response.body().toAchievement()
        _achievements.update { list ->
            list.map { if (it.id == updatedAchievement.id) updatedAchievement else it }
        }
    }

    override suspend fun deleteAchievementById(id: String) {
        val response = achievementsApi.deleteAchievementAchievementsAchievementIdDelete(id)
        response.check()
        _achievements.update { list -> list.filter { it.id != id } }
    }

    override fun invalidateCache(ids: Collection<String>) {
        _achievements.update { list -> list.filter { it.id !in ids } }
    }
}
