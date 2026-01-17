package com.plezha.achi.shared.data.network

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.model.AchievementStep
import com.plezha.achi.shared.data.model.StepProgress
import com.plezha.achi.shared.data.network.models.AchievementPackSchema
import com.plezha.achi.shared.data.network.models.AchievementSchema
import com.plezha.achi.shared.data.network.models.AchievementStepSchema

/**
 * Maps network schema models to domain models.
 */

fun AchievementPackSchema.toAchievementPack() = AchievementPack(
    id = id,
    name = name,
    count = count,
    achievementIds = achievementIds,
    previewImageUrl = previewImageUrl,
    code = code
)

fun AchievementSchema.toAchievement() = Achievement(
    id = id,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    steps = steps.map { it.toAchievementStep() },
    previewImageUrl = previewImageUrl,
    imageUrl = imageUrl
)

fun AchievementStepSchema.toAchievementStep() = AchievementStep(
    description = description,
    progress = StepProgress(0, substepsAmount ?: 1)
)

fun com.plezha.achi.shared.data.network.models.StepProgress.toDomainStepProgress() = StepProgress(
    substepsDone ?: 0,
    substepsAmount ?: 1
)
