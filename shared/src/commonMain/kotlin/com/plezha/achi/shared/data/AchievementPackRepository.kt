package com.plezha.achi.shared.data

import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.data.network.infrastructure.HttpResponse
import com.plezha.achi.shared.data.network.models.AchievementCreateBody
import com.plezha.achi.shared.data.network.models.AchievementPackCreateBody
import com.plezha.achi.shared.data.network.models.AchievementPackSchema
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.InputProvider
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.Buffer
import kotlinx.io.buffered

interface AchievementPackRepository {
    val achievementPacks: StateFlow<List<AchievementPack>>

    suspend fun getAchievementPackByCode(code: String): AchievementPack

    suspend fun getAchievementPackById(id: String): AchievementPack

    suspend fun createAchievementPack(
        name: String,
        achievements: List<AchievementCreateBody>,
        imageBytes: ByteArray,
        imageFileName: String,
        achievementImages: Map<Int, Pair<ByteArray, String>> = emptyMap()
    ): String

    suspend fun editAchievementPack(pack: AchievementPack)
}

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

    // Returns id
    override suspend fun createAchievementPack(
        name: String,
        achievements: List<AchievementCreateBody>,
        imageBytes: ByteArray,
        imageFileName: String,
        achievementImages: Map<Int, Pair<ByteArray, String>>
    ): String {
        val ids = ConcurrentSet<String>()
        
        val achievementsWithImages = achievements.mapIndexed { index, achievement ->
            val imageData = achievementImages[index]
            if (imageData != null) {
                val (imgBytes, imgFileName) = imageData
                val formPart = createFormPart(imgBytes, imgFileName)
                val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "achievement-images")
                uploadResponse.check()
                val imageUrl = uploadResponse.body().toString()
                achievement.copy(
                    imageUrl = imageUrl,
                    previewImageUrl = imageUrl
                )
            } else {
                achievement
            }
        }
        
        for (achievement in achievementsWithImages) {
            // TODO UseCases
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
        val packPreviewImageUrl = uploadResponse.body().toString()

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
        
        return createdPack.id
    }

    override suspend fun editAchievementPack(pack: AchievementPack) {
        TODO("Not yet implemented")
    }
}


// TODO() transfer to utils
fun createFormPart(
    imageBytes: ByteArray,
    imageFileName: String,
): FormPart<InputProvider> =
    FormPart(
        key = "image",
        value = InputProvider { 
            Buffer().apply { write(imageBytes) }
        },
        headers = Headers.build {
            append(HttpHeaders.ContentDisposition, "filename=$imageFileName")
        }
    )

fun <T : Any> HttpResponse<T>.check() {
    if (!success) {
        throw Exception("Network error $response")
    }
}

private fun AchievementPackSchema.toAchievementPack() = AchievementPack(
    id = id,
    name = name,
    count = count,
    achievementIds = achievementIds,
    previewImageUrl = previewImageUrl,
    code = code
)