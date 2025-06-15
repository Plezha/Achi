package com.plezha.achi.shared.data

interface AchievementPackRepository {
    suspend fun getAchievementPacks(): List<AchievementPack>
}

class MockAchievementPackRepository : AchievementPackRepository {
    override suspend fun getAchievementPacks(): List<AchievementPack> {
        return listOf(
            AchievementPack("Pack 1", 5),
        )
    }
}