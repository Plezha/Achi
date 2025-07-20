package com.plezha.achi.shared.data.model

data class AchievementPack(
    val name: String,
    val count: Int,
    val achievements: List<Achievement>
)

