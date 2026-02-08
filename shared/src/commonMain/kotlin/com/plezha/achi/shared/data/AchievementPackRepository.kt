package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.data.network.models.AchievementCreateBody
import com.plezha.achi.shared.data.network.models.AchievementPackCreateBody
import com.plezha.achi.shared.data.network.models.AchievementPackUpdateBody
import com.plezha.achi.shared.data.network.models.AchievementStepCreate
import com.plezha.achi.shared.data.network.models.AchievementUpdateBody
import com.plezha.achi.shared.data.network.toAchievementPack
import com.plezha.achi.shared.data.network.check
import com.plezha.achi.shared.data.network.createFormPart
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface AchievementPackRepository {
    val achievementPacks: StateFlow<List<AchievementPack>>

    suspend fun getAchievementPackByCode(code: String): AchievementPack

    suspend fun getAchievementPackById(id: String): AchievementPack

    /**
     * Creates a new achievement pack on the server.
     * @return The created pack (including its code for sharing)
     */
    suspend fun createAchievementPack(
        name: String,
        achievements: List<AchievementCreateBody>,
        imageBytes: ByteArray,
        imageFileName: String,
        achievementImages: Map<Int, Pair<ByteArray, String>> = emptyMap()
    ): AchievementPack

    /**
     * Updates an existing achievement pack on the server.
     * Handles creating/updating/deleting achievements and uploading new images.
     * @return The updated pack
     */
    suspend fun updateAchievementPack(
        packId: String,
        name: String,
        achievements: List<AchievementEditData>,
        originalAchievementIds: List<String>,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null,
        achievementImages: Map<Int, Pair<ByteArray, String>> = emptyMap()
    ): AchievementPack

    /**
     * Deletes an achievement pack from the server.
     */
    suspend fun deleteAchievementPack(packId: String)

    /** Removes a cached pack so the next [getAchievementPackById] call fetches fresh data from the server. */
    fun invalidatePackCache(packId: String)

    /**
     * Creates a copy of an existing pack owned by the current user.
     * Duplicates all achievements (reusing existing image URLs) and creates a new pack.
     * @return The newly created pack
     */
    suspend fun copyPack(
        originalPack: AchievementPack,
        achievements: List<Achievement>
    ): AchievementPack
}

/**
 * Data for creating or updating an achievement within a pack update.
 * If [serverId] is non-null, the achievement already exists and will be updated.
 * If [serverId] is null, a new achievement will be created.
 */
data class AchievementEditData(
    val serverId: String?,
    val title: String,
    val shortDescription: String,
    val longDescription: String?,
    val steps: List<AchievementStepEditData>
)

data class AchievementStepEditData(
    val description: String,
    val substepsAmount: Int
)

class AchievementPackRepositoryImpl(
    val achievementsApi: AchievementsApi,
    val packsApi: PacksApi,
    val uploadApi: UploadApi
) : AchievementPackRepository {
    private val _packs = MutableStateFlow<List<AchievementPack>>(emptyList())
    override val achievementPacks = _packs.asStateFlow()

    override suspend fun getAchievementPackByCode(code: String): AchievementPack {
        try {
            val response = packsApi.getPackPacksPackCodeGet(code)
            response.check()
            val newPack: AchievementPack = response.body().toAchievementPack()

            _packs.update { currentPacks -> // TODO that's viewmodel logic?
                val alreadyAddedPack = currentPacks.find { it.id == newPack.id }
                if (alreadyAddedPack != null) {
                    throw IllegalStateException(
                        "Pack \"${alreadyAddedPack.name}\" is already in the list"
                    )
                } else {
                    currentPacks + newPack
                }
            }
            return newPack
        } catch (e: Exception) {
            throw e // TODO
        }
    }

    override suspend fun getAchievementPackById(id: String): AchievementPack {
        val ramPack = _packs.value.find { it.id == id }
        if (ramPack != null) {
            return ramPack
        } else {

            val resp = packsApi.getPackByIdPacksByIdPackIdGet(id)
            resp.check()
            return resp.body().toAchievementPack()
        }
    }

    override suspend fun createAchievementPack(
        name: String,
        achievements: List<AchievementCreateBody>,
        imageBytes: ByteArray,
        imageFileName: String,
        achievementImages: Map<Int, Pair<ByteArray, String>>
    ): AchievementPack {
        val ids = ConcurrentSet<String>()
        
        val achievementsWithImages = achievements.mapIndexed { index, achievement ->
            val imageData = achievementImages[index]
            if (imageData != null) {
                val (imgBytes, imgFileName) = imageData
                val formPart = createFormPart(imgBytes, imgFileName)
                val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "achievement-images")
                uploadResponse.check()
                val imageUrl = uploadResponse.body().url
                achievement.copy(
                    imageUrl = imageUrl,
                    previewImageUrl = imageUrl
                )
            } else {
                achievement
            }
        }
        
        for (achievement in achievementsWithImages) {
            val response =
                achievementsApi.createAchievementAchievementsPost(
                    achievement
                )
            response.check()
            ids.add(response.body().id)
        }

        val formPart = createFormPart(imageBytes, imageFileName)

        val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "pack-previews")
        uploadResponse.check()
        val packPreviewImageUrl = uploadResponse.body().url

        val response = packsApi.createPackPacksPost(
            AchievementPackCreateBody(
                name = name,
                achievementIds = ids.toList(),
                previewImageUrl = packPreviewImageUrl
            )
        )
        response.check()
        
        val createdPack = response.body().toAchievementPack()
        _packs.update { currentPacks -> currentPacks + createdPack }
        
        return createdPack
    }

    override suspend fun updateAchievementPack(
        packId: String,
        name: String,
        achievements: List<AchievementEditData>,
        originalAchievementIds: List<String>,
        imageBytes: ByteArray?,
        imageFileName: String?,
        achievementImages: Map<Int, Pair<ByteArray, String>>
    ): AchievementPack {
        val updatedAchievementIds = mutableListOf<String>()

        // Create or update achievements
        achievements.forEachIndexed { index, achievement ->
            val steps = achievement.steps.map { step ->
                AchievementStepCreate(
                    description = step.description,
                    substepsAmount = step.substepsAmount
                )
            }

            // Upload achievement image if provided
            var achievementImageUrl: String? = null
            val imageData = achievementImages[index]
            if (imageData != null) {
                val (imgBytes, imgFileName) = imageData
                val formPart = createFormPart(imgBytes, imgFileName)
                val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "achievement-images")
                uploadResponse.check()
                achievementImageUrl = uploadResponse.body().url
            }

            if (achievement.serverId != null) {
                // Update existing achievement
                val updateBody = AchievementUpdateBody(
                    title = achievement.title,
                    shortDescription = achievement.shortDescription,
                    longDescription = achievement.longDescription,
                    steps = steps,
                    imageUrl = achievementImageUrl,
                    previewImageUrl = achievementImageUrl
                )
                val response = achievementsApi.updateAchievementAchievementsAchievementIdPut(
                    achievementId = achievement.serverId,
                    achievementUpdateBody = updateBody
                )
                response.check()
                updatedAchievementIds.add(achievement.serverId)
            } else {
                // Create new achievement
                val createBody = AchievementCreateBody(
                    title = achievement.title,
                    shortDescription = achievement.shortDescription,
                    steps = steps,
                    longDescription = achievement.longDescription,
                    imageUrl = achievementImageUrl,
                    previewImageUrl = achievementImageUrl
                )
                val response = achievementsApi.createAchievementAchievementsPost(createBody)
                response.check()
                updatedAchievementIds.add(response.body().id)
            }
        }

        // Delete removed achievements (best-effort)
        val removedIds = originalAchievementIds.filter { it !in updatedAchievementIds }
        for (removedId in removedIds) {
            try {
                achievementsApi.deleteAchievementAchievementsAchievementIdDelete(removedId).check()
            } catch (_: Exception) { }
        }

        // Upload new pack preview image if provided
        val previewImageUrl = if (imageBytes != null && imageFileName != null) {
            val formPart = createFormPart(imageBytes, imageFileName)
            val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "pack-previews")
            uploadResponse.check()
            uploadResponse.body().url
        } else {
            null
        }

        // Update the pack itself
        val response = packsApi.updatePackPacksByIdPackIdPut(
            packId = packId,
            achievementPackUpdateBody = AchievementPackUpdateBody(
                name = name,
                achievementIds = updatedAchievementIds,
                previewImageUrl = previewImageUrl
            )
        )
        response.check()

        val updatedPack = response.body().toAchievementPack()
        _packs.update { currentPacks ->
            currentPacks.map { if (it.id == updatedPack.id) updatedPack else it }
        }

        return updatedPack
    }

    override suspend fun deleteAchievementPack(packId: String) {
        val response = packsApi.deletePackPacksByIdPackIdDelete(packId)
        response.check()
        _packs.update { currentPacks -> currentPacks.filter { it.id != packId } }
    }

    override fun invalidatePackCache(packId: String) {
        _packs.update { currentPacks -> currentPacks.filter { it.id != packId } }
    }

    override suspend fun copyPack(
        originalPack: AchievementPack,
        achievements: List<Achievement>
    ): AchievementPack {
        // Create copies of each achievement, reusing existing image URLs
        val newAchievementIds = mutableListOf<String>()
        for (achievement in achievements) {
            val steps = achievement.steps.map { step ->
                AchievementStepCreate(
                    description = step.description,
                    substepsAmount = step.progress.substepsAmount
                )
            }
            val createBody = AchievementCreateBody(
                title = achievement.title,
                shortDescription = achievement.shortDescription,
                longDescription = achievement.longDescription,
                steps = steps,
                previewImageUrl = achievement.previewImageUrl,
                imageUrl = achievement.imageUrl
            )
            val response = achievementsApi.createAchievementAchievementsPost(createBody)
            response.check()
            newAchievementIds.add(response.body().id)
        }

        // Create the new pack with the copied achievement IDs
        val response = packsApi.createPackPacksPost(
            AchievementPackCreateBody(
                name = originalPack.name,
                achievementIds = newAchievementIds,
                previewImageUrl = originalPack.previewImageUrl
            )
        )
        response.check()

        val copiedPack = response.body().toAchievementPack()
        _packs.update { currentPacks -> currentPacks + copiedPack }
        return copiedPack
    }
}