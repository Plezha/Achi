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

// Base URL for resolving relative image URLs
// TODO: This should ideally come from a config/DI
private const val IMAGE_BASE_URL = "http://10.0.2.2:8000"

/**
 * Resolves an image URL - prepends base URL if the path is relative
 */
private fun resolveImageUrl(url: String?): String? {
    if (url == null) return null
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "$IMAGE_BASE_URL$url"
    }
}

fun AchievementPackSchema.toAchievementPack() = AchievementPack(
    id = id,
    name = name,
    count = count,
    achievementIds = achievementIds,
    previewImageUrl = resolveImageUrl(previewImageUrl),
    code = code
)

fun AchievementSchema.toAchievement() = Achievement(
    id = id,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    steps = steps.map { it.toAchievementStep() },
    previewImageUrl = resolveImageUrl(previewImageUrl),
    imageUrl = resolveImageUrl(imageUrl)
)

fun AchievementStepSchema.toAchievementStep() = AchievementStep(
    description = description,
    progress = StepProgress(0, substepsAmount ?: 1)
)

fun com.plezha.achi.shared.data.network.models.StepProgress.toDomainStepProgress() = StepProgress(
    substepsDone ?: 0,
    substepsAmount ?: 1
)
