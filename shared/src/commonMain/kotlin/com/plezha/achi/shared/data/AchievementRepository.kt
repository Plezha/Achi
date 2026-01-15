package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementStep
import com.plezha.achi.shared.data.model.StepProgress
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.models.AchievementSchema
import com.plezha.achi.shared.data.network.models.AchievementStepSchema
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

class MockAchievementRepository(
    private val achievementsApi: AchievementsApi,
) : AchievementRepository {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    override val achievements = _achievements.asStateFlow()

    override suspend fun getAchievement(id: String): Achievement {
        try {
            val ramAchievement = _achievements.value.find { it.id == id }
            if (ramAchievement != null) {
                return ramAchievement
            }
            val response = achievementsApi.getAchievementAchievementsAchievementIdGet(id)
            response.check()
            val achievement = response.body().toAchievement()
            _achievements.update { it + achievement }
            return achievement
        } catch (e: Exception) {
            throw e // TODO
        }
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

fun AchievementSchema.toAchievement() = Achievement(
    id = id,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    steps = steps.map { it.toAchievementStep() } ,
    previewImageUrl = previewImageUrl,
    imageUrl = imageUrl
)

fun AchievementStepSchema.toAchievementStep() = AchievementStep(
    description = description,
    progress = /* stepProgress?.toDomainStepProgress() ?: */ StepProgress(0, 1)
)

fun com.plezha.achi.shared.data.network.models.StepProgress.toDomainStepProgress() = StepProgress(
    substepsDone ?: 0,
    substepsAmount ?: 1
)

val achievementExample = Achievement(
    id = "1",
    title = "САПР",
    shortDescription = "Системный Анализ и Принятие Решений",
    steps = listOf(
        AchievementStep(description = "Первая лаба принята"),
        AchievementStep(description = "Вторая лаба принята"),
        AchievementStep(description = "Третья лаба принята"),
        AchievementStep(description = "Четвертая лаба принята"),
        AchievementStep(description = "Пятая лаба принята"),
        AchievementStep(description = "Шестая лаба принята"),
        AchievementStep(
            description = "Подготовиться хоть чуть чуть к экзу",
            progress = StepProgress(0, 10)
        ),
        AchievementStep(description = "В зачётке зачёт"),
    ),
)