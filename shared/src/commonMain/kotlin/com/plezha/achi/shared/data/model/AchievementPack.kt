package com.plezha.achi.shared.data.model

data class AchievementPack(
    val id: String,
    val name: String,
    val count: Int,
    val achievementIds: List<String>,
    val previewImageUrl: String? = null,
    val code: String
)
