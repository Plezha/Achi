# API Integration Documentation

## Overview

The Achi application integrates with a backend API for achievement and pack management, user authentication, and progress tracking. API client code is auto-generated from an OpenAPI specification using the OpenAPI Generator Gradle plugin, and network requests are made using Ktor Client.

## OpenAPI Specification

### Specification File

**Location**: `Achi_openapi.json` (project root)

**OpenAPI Version**: 3.1.0

**API Title**: "KMP Achievements Backend"

### Server Configuration

**Production/Public Server**:
- URL: Configured via ngrok tunnel (see spec)
- Description: Production/Public Server

**Local Development Server**:
- URL: `http://127.0.0.1:8000`
- Description: Local Development Server

### API Endpoints

#### Authentication API

**Tag**: Authentication

**POST /login/token**
- **Operation ID**: `login_token_post`
- **Purpose**: User login, returns access token
- **Request**: Form data with username and password
- **Response**: Token with `accessToken` field
- **Authentication**: Not required (provides tokens)

#### Users API

**Tag**: Users

**POST /users**
- **Operation ID**: `register_user_users_post`
- **Purpose**: Register new user
- **Request Body**: `UserCreateBody` (username, password, displayName)
- **Response**: Created user schema
- **Authentication**: Not required

#### User Collection API

**Tag**: UserCollection

**GET /user/packs**
- **Purpose**: Get user's pack collection
- **Response**: List of packs in user's collection
- **Authentication**: Required

**POST /user/packs/{pack_code}**
- **Purpose**: Add pack to user's collection by code
- **Path Parameter**: `pack_code` (string)
- **Response**: Added pack schema
- **Authentication**: Required

**DELETE /user/packs/{pack_id}**
- **Purpose**: Remove pack from user's collection
- **Path Parameter**: `pack_id` (string)
- **Authentication**: Required

#### User Progress API

**Tag**: UserProgress

**GET /user/progress/batch**
- **Purpose**: Get all achievement progress for user
- **Response**: Batch of all progress records
- **Authentication**: Required

**GET /user/progress/{achievement_id}**
- **Purpose**: Get progress for specific achievement
- **Path Parameter**: `achievement_id` (string)
- **Response**: Progress with step states
- **Authentication**: Required

**PATCH /user/progress/{achievement_id}/steps/{step_id}**
- **Purpose**: Update step progress
- **Path Parameters**: `achievement_id`, `step_id` (stable step ID string)
- **Request Body**: `StepProgressUpdateBody` with `substepsDone`
- **Response**: Updated progress
- **Authentication**: Required

**PATCH /user/progress/{achievement_id}/complete**
- **Purpose**: Toggle achievement completion status (for stepless achievements)
- **Path Parameter**: `achievement_id` (string)
- **Request Body**: `AchievementCompletionBody` with `isCompleted` (boolean)
- **Response**: Updated progress
- **Authentication**: Required

#### Achievements API

**Tag**: Achievements

**POST /achievements**
- **Purpose**: Create a new achievement
- **Request Body**: `AchievementCreateBody`
- **Response**: `AchievementSchema` (201 Created)
- **Authentication**: Required

**GET /achievements/{achievement_id}**
- **Purpose**: Retrieve a specific achievement by ID
- **Path Parameter**: `achievement_id` (string)
- **Response**: `AchievementSchema` (200 OK)
- **Authentication**: Not required (public)

#### Packs API

**Tag**: Packs

**POST /packs**
- **Purpose**: Create a new achievement pack
- **Request Body**: `AchievementPackCreateBody`
- **Response**: `AchievementPackSchema` (201 Created)
- **Authentication**: Required

**GET /packs/{pack_code}**
- **Purpose**: Get pack by shareable code
- **Path Parameter**: `pack_code` (string)
- **Response**: `AchievementPackSchema`
- **Authentication**: Not required (public)

**GET /packs-by-id/{pack_id}**
- **Purpose**: Get pack by ID
- **Path Parameter**: `pack_id` (string)
- **Response**: `AchievementPackSchema`
- **Authentication**: Not required (public)

#### Upload API

**Tag**: Upload

**POST /upload/image**
- **Purpose**: Upload an image and return its URL
- **Request**: `multipart/form-data` with image file
- **Parameters**: 
  - `image`: File to upload
  - `subdir`: Subdirectory (e.g., "pack-previews", "achievement-images")
- **Response**: Image upload response with URL
- **Authentication**: Required

### Security

**Authentication Scheme**: OAuth2PasswordBearer

- Token-based authentication
- Bearer token in Authorization header
- Required for: Create, Update, Delete operations, User-specific endpoints
- Not required for: Read operations on public endpoints

## Code Generation

### OpenAPI Generator Plugin

**Plugin**: `org.openapi.generator` version 7.18.0

**Configuration** (in `shared/build.gradle.kts`):

```kotlin
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(rootProject.file("Achi_openapi.json").toURI().toString())
    outputDir.set("${buildDir}/generated/openapi")
    packageName.set("com.plezha.achi.shared.data.network")
    
    configOptions.set(mapOf(
        "library" to "multiplatform",
        "useCoroutines" to "true",
        "dateLibrary" to "kotlinx-datetime",
        "sourceFolder" to "src/commonMain/kotlin"
    ))
}
```

### Generated Code Location

**Output Directory**: `shared/build/generated/openapi/`

**Package**: `com.plezha.achi.shared.data.network`

**Source Integration**:
```kotlin
kotlin.srcDir("${buildDir}/generated/openapi/src/commonMain/kotlin")
```

### Generated Artifacts

**APIs** (`data/network/apis/`):
- `AchievementsApi.kt`
- `AuthenticationApi.kt`
- `PacksApi.kt`
- `UploadApi.kt`
- `UserCollectionApi.kt`
- `UserProgressApi.kt`
- `UsersApi.kt`

**Models** (`data/network/models/`):
- `AchievementSchema.kt`
- `AchievementCreateBody.kt`
- `AchievementStepSchema.kt`
- `AchievementStepCreate.kt`
- `AchievementPackSchema.kt`
- `AchievementPackCreateBody.kt`
- `StepProgress.kt`
- `StepProgressUpdateBody.kt`
- `UserAchievementProgress.kt`
- `UserCreateBody.kt`
- `Token.kt`
- Error models, etc.

### Build Integration

```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}
```

## Ktor Client Configuration

### HTTP Client Setup

**Ktor Version**: 3.1.0

**Platform-Specific Engines**:

**Android** (`App.android.kt`):
```kotlin
actual val httpClientEngine: HttpClientEngine
    get() = OkHttp.create()
```

**WasmJS** (`App.wasmJs.kt`):
```kotlin
actual val httpClientEngine: HttpClientEngine
    get() = CIO.create()
```

### Ktor Dependencies

**Common** (`commonMain`):
```kotlin
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
implementation(libs.ktor.client.logging)
```

**Android** (`androidMain`):
```kotlin
implementation(libs.ktor.client.okhttp)
```

**WasmJS** (`wasmJsMain`):
```kotlin
implementation(libs.ktor.client.cio)
```

## DI and API Configuration

### AppModule

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/di/AppModule.kt`

```kotlin
class AppModule(httpClientEngine: HttpClientEngine) {
    // API Clients - created with base URL and engine
    val usersApi = UsersApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val authApi = AuthenticationApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val achievementsApi = AchievementsApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val packsApi = PacksApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val uploadApi = UploadApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val userCollectionApi = UserCollectionApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val userProgressApi = UserProgressApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    
    // Repositories use the APIs
    val authRepository: AuthRepository by lazy { ... }
}
```

### ApiConfig

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/di/ApiConfig.kt`

```kotlin
object ApiConfig {
    private const val LOCALHOST_ANDROID_EMULATOR = "http://10.0.2.2:8000"
    
    val baseUrl: String = ApiClient.BASE_URL  // From OpenAPI spec
}
```

## Authentication

### AuthRepository

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/auth/AuthRepository.kt`

**Responsibilities**:
- Login/logout state management
- Token persistence via multiplatform-settings
- Token injection to all APIs

**Session Restoration**:
```kotlin
init {
    restoreSession()
}

private fun restoreSession() {
    val savedToken = settings.getStringOrNull(KEY_ACCESS_TOKEN)
    val savedUsername = settings.getStringOrNull(KEY_USERNAME)
    
    if (savedToken != null && savedUsername != null) {
        setTokenOnApis(savedToken)
        _authState.value = AuthState(
            isLoggedIn = true,
            username = savedUsername,
            accessToken = savedToken
        )
    }
}
```

**Token Injection**:
```kotlin
private fun setTokenOnApis(token: String) {
    achievementsApi.setAccessToken(token)
    packsApi.setAccessToken(token)
    uploadApi.setAccessToken(token)
    userCollectionApi.setAccessToken(token)
    userProgressApi.setAccessToken(token)
}
```

**Login Flow**:
```kotlin
suspend fun login(username: String, password: String): AuthResult {
    val response = authApi.loginTokenPost(username, password)
    
    if (response.success) {
        val token = response.body().accessToken
        
        // Save to settings
        settings.putString(KEY_ACCESS_TOKEN, token)
        settings.putString(KEY_USERNAME, username)
        
        // Set token on APIs
        setTokenOnApis(token)
        
        return AuthResult.Success(username)
    }
    return AuthResult.Error("Login failed")
}
```

## Repository Integration

### Network Utils

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/network/NetworkUtils.kt`

**Response Checking**:
```kotlin
fun <T : Any> HttpResponse<T>.check() {
    if (!success) {
        throw Exception("Network error $response")
    }
}
```

**File Upload**:
```kotlin
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
```

### Mappers

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/network/Mappers.kt`

**Image URL Resolution**:
```kotlin
private fun resolveImageUrl(url: String?): String? {
    if (url == null) return null
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "${ApiConfig.baseUrl}$url"
    }
}
```

**Schema to Domain Mapping**:
```kotlin
fun AchievementSchema.toAchievement() = Achievement(
    id = id,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    steps = steps.map { it.toAchievementStep() },
    previewImageUrl = resolveImageUrl(previewImageUrl),
    imageUrl = resolveImageUrl(imageUrl)
)
```

### UserRepository

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/UserRepository.kt`

**User's Pack Collection**:
```kotlin
override suspend fun loadUserPacks() {
    val response = userCollectionApi.getUserPacksUserPacksGet()
    response.check()
    val packs = response.body().packs.map { it.toAchievementPack() }
    _userPacks.value = packs
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
```

**Progress Sync**:
```kotlin
override suspend fun updateStepProgress(
    achievementId: String,
    stepId: String,
    substepsDone: Int
): UserAchievementProgress {
    val response = userProgressApi.updateStepProgressUserProgressAchievementIdStepsStepIdPatch(
        achievementId = achievementId,
        stepId = stepId,
        stepProgressUpdateBody = StepProgressUpdateBody(substepsDone = substepsDone)
    )
    response.check()
    val updatedProgress = response.body()
    
    // Update cache
    _progressCache.update { it + (achievementId to updatedProgress) }
    
    return updatedProgress
}
```

### AchievementPackRepository

**Pack Creation with Image Upload**:
```kotlin
override suspend fun createAchievementPack(
    name: String,
    achievements: List<AchievementCreateBody>,
    imageBytes: ByteArray,
    imageFileName: String,
    achievementImages: Map<Int, Pair<ByteArray, String>>
): AchievementPack {
    // 1. Upload achievement images and update achievement bodies
    val achievementsWithImages = achievements.mapIndexed { index, achievement ->
        val imageData = achievementImages[index]
        if (imageData != null) {
            val (imgBytes, imgFileName) = imageData
            val formPart = createFormPart(imgBytes, imgFileName)
            val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "achievement-images")
            uploadResponse.check()
            val imageUrl = uploadResponse.body().url
            achievement.copy(imageUrl = imageUrl, previewImageUrl = imageUrl)
        } else {
            achievement
        }
    }
    
    // 2. Create achievements and collect IDs
    val ids = mutableListOf<String>()
    for (achievement in achievementsWithImages) {
        val response = achievementsApi.createAchievementAchievementsPost(achievement)
        response.check()
        ids.add(response.body().id)
    }
    
    // 3. Upload pack preview image
    val formPart = createFormPart(imageBytes, imageFileName)
    val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "pack-previews")
    uploadResponse.check()
    val packPreviewImageUrl = uploadResponse.body().url
    
    // 4. Create pack
    val response = packsApi.createPackPacksPost(
        AchievementPackCreateBody(
            name = name,
            achievementIds = ids,
            previewImageUrl = packPreviewImageUrl
        )
    )
    response.check()
    
    return response.body().toAchievementPack()
}
```

## Error Handling

### Response Checking
```kotlin
val response = api.someCall()
response.check()  // Throws if !response.success
val data = response.body()
```

### ViewModel Error Handling
```kotlin
try {
    val pack = userRepository.addPackByCode(code)
    _messageChannel.send("${pack.name} added successfully")
} catch (e: Exception) {
    val errorMessage = when {
        e.message?.contains("409") == true -> "Already in collection"
        e.message?.contains("404") == true -> "Pack not found"
        e.message?.contains("401") == true -> "Please log in"
        else -> e.message ?: "Unknown error"
    }
    _messageChannel.send(errorMessage)
}
```

## Dependencies

### Version Catalog

```toml
[versions]
ktor = "3.1.0"
kotlinxSerializationJson = "1.9.0"
kotlinxDatetime = "0.7.0"
multiplatformSettings = "1.3.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatformSettings" }
```

## Best Practices

1. **Always check responses**: Use `.check()` extension
2. **Handle auth errors**: Check for 401 and prompt login
3. **Map to domain models**: Don't expose network models to UI
4. **Optimistic updates**: Update UI immediately, sync in background
5. **Cache where appropriate**: Store in StateFlow to reduce API calls
6. **Use suspend functions**: All API calls are suspending
7. **Inject HTTP engine**: Use platform-appropriate engine
8. **Centralize API config**: Use ApiConfig for base URL
