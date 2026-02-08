package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.model.StepProgress
import com.plezha.achi.shared.data.network.apis.UserCollectionApi
import com.plezha.achi.shared.data.network.apis.UserProgressApi
import com.plezha.achi.shared.data.network.check
import com.plezha.achi.shared.data.network.models.AchievementCompletionBody
import com.plezha.achi.shared.data.network.models.StepProgressUpdateBody
import com.plezha.achi.shared.data.network.models.UserAchievementProgress
import com.plezha.achi.shared.data.network.toAchievementPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository for managing user's pack collection and achievement progress.
 * All data is synced with the server - requires authentication.
 */
interface UserRepository {
    /** User's current pack collection */
    val userPacks: StateFlow<List<AchievementPack>>
    
    /** Whether data is currently being loaded */
    val isLoading: StateFlow<Boolean>
    
    /** Cached progress for all achievements */
    val progressCache: StateFlow<Map<String, UserAchievementProgress>>
    
    /** Load user's packs from server */
    suspend fun loadUserPacks()
    
    /** Add a pack to user's collection by code */
    suspend fun addPackByCode(code: String): AchievementPack
    
    /** Remove a pack from user's collection */
    suspend fun removePackFromCollection(packId: String)
    
    /** Load all progress from server */
    suspend fun loadAllProgress()
    
    /** Get progress for a specific achievement (from cache or server) */
    suspend fun getProgress(achievementId: String): UserAchievementProgress?
    
    /** Update step progress and sync to server */
    suspend fun updateStepProgress(achievementId: String, stepIndex: Int, substepsDone: Int): UserAchievementProgress
    
    /** Update achievement completion status (for stepless achievements) */
    suspend fun updateAchievementCompletion(achievementId: String, isCompleted: Boolean): UserAchievementProgress
    
    /** Clear all cached data (call on logout) */
    fun clearCache()
}

class UserRepositoryImpl(
    private val userCollectionApi: UserCollectionApi,
    private val userProgressApi: UserProgressApi
) : UserRepository {
    
    private val _userPacks = MutableStateFlow<List<AchievementPack>>(emptyList())
    override val userPacks: StateFlow<List<AchievementPack>> = _userPacks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _progressCache = MutableStateFlow<Map<String, UserAchievementProgress>>(emptyMap())
    override val progressCache: StateFlow<Map<String, UserAchievementProgress>> = _progressCache.asStateFlow()
    
    override suspend fun loadUserPacks() {
        _isLoading.value = true
        try {
            val response = userCollectionApi.getUserPacksUserPacksGet()
            response.check()
            val packs = response.body().packs.map { it.toAchievementPack() }
            _userPacks.value = packs
        } finally {
            _isLoading.value = false
        }
    }
    
    override suspend fun addPackByCode(code: String): AchievementPack {
        val response = userCollectionApi.addPackToCollectionUserPacksPackCodePost(code)
        response.check()
        val newPack = response.body().toAchievementPack()
        
        // Update local cache
        _userPacks.update { currentPacks ->
            if (currentPacks.none { it.id == newPack.id }) {
                currentPacks + newPack
            } else {
                currentPacks
            }
        }
        
        return newPack
    }
    
    override suspend fun removePackFromCollection(packId: String) {
        val response = userCollectionApi.removePackFromCollectionUserPacksPackIdDelete(packId)
        response.check()
        
        // Update local cache
        _userPacks.update { currentPacks ->
            currentPacks.filter { it.id != packId }
        }
    }
    
    override suspend fun loadAllProgress() {
        val response = userProgressApi.getAllProgressUserProgressBatchGet()
        response.check()
        
        val progressMap = response.body().progress.associateBy { it.achievementId }
        _progressCache.value = progressMap
    }
    
    override suspend fun getProgress(achievementId: String): UserAchievementProgress? {
        // Check cache first
        _progressCache.value[achievementId]?.let { return it }
        
        // Fetch from server
        return try {
            val response = userProgressApi.getAchievementProgressUserProgressAchievementIdGet(achievementId)
            response.check()
            val progress = response.body()
            
            // Update cache
            _progressCache.update { it + (achievementId to progress) }
            
            progress
        } catch (e: Exception) {
            // No progress recorded yet, return null
            null
        }
    }
    
    override suspend fun updateStepProgress(
        achievementId: String,
        stepIndex: Int,
        substepsDone: Int
    ): UserAchievementProgress {
        val response = userProgressApi.updateStepProgressUserProgressAchievementIdStepsStepIndexPatch(
            achievementId = achievementId,
            stepIndex = stepIndex,
            stepProgressUpdateBody = StepProgressUpdateBody(substepsDone = substepsDone)
        )
        response.check()
        val updatedProgress = response.body()
        
        // Update cache
        _progressCache.update { it + (achievementId to updatedProgress) }
        
        return updatedProgress
    }
    
    override suspend fun updateAchievementCompletion(
        achievementId: String,
        isCompleted: Boolean
    ): UserAchievementProgress {
        val response = userProgressApi.toggleAchievementCompletionUserProgressAchievementIdCompletePatch(
            achievementId = achievementId,
            achievementCompletionBody = AchievementCompletionBody(isCompleted = isCompleted)
        )
        response.check()
        val updatedProgress = response.body()
        
        // Update cache
        _progressCache.update { it + (achievementId to updatedProgress) }
        
        return updatedProgress
    }
    
    override fun clearCache() {
        _userPacks.value = emptyList()
        _progressCache.value = emptyMap()
    }
}

/**
 * Extension to convert server progress to domain StepProgress list
 */
fun UserAchievementProgress.toStepProgressList(): List<StepProgress> {
    return steps.map { serverStep ->
        StepProgress(
            substepsDone = serverStep.substepsDone ?: 0,
            substepsAmount = serverStep.substepsAmount ?: 1
        )
    }
}
