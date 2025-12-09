# API Integration Documentation

## Overview

The Achi application integrates with a backend API for achievement and pack management. API client code is auto-generated from an OpenAPI specification using the OpenAPI Generator Gradle plugin, and network requests are made using Ktor Client.

## OpenAPI Specification

### Specification File

**Location**: `Achi_openapi.json` (project root)

**OpenAPI Version**: 3.1.0

**API Title**: "KMP Achievements Backend"

**API Version**: 0.1.0

### Server Configuration

Two server environments defined:

**Production/Public Server**:
- URL: `https://lucky-eminent-gator.ngrok-free.app`
- Description: Production/Public Server (via ngrok)
- Tunneled endpoint for public access

**Local Development Server**:
- URL: `http://127.0.0.1:8000`
- Description: Local Development Server
- For local backend development

### API Endpoints

#### Upload API

**Tag**: Upload

**POST /upload/image**
- **Operation ID**: `upload_image_upload_image_post`
- **Purpose**: Upload an image and return its URL
- **Request**: `multipart/form-data` with image file
- **Parameters**: 
  - `image`: File to upload
  - `subdir`: Subdirectory to save to (e.g., "pack-previews")
- **Response**: Image URL string
- **Authentication**: Required (OAuth2PasswordBearer)
- **Saves to**: `static/images/{subdir}/`

#### Achievements API

**Tag**: Achievements

**POST /achievements**
- **Operation ID**: `create_achievement_achievements_post`
- **Purpose**: Create a new achievement
- **Request Body**: `AchievementCreateBody`
  ```json
  {
    "title": "string",
    "shortDescription": "string",
    "steps": [{"description": "string", "substepsAmount": 1}],
    "longDescription": "string (optional)",
    "previewImageUrl": "string (optional)",
    "imageUrl": "string (optional)"
  }
  ```
- **Response**: `AchievementSchema` (201 Created)
- **Authentication**: Required

**GET /achievements/{achievement_id}**
- **Operation ID**: `get_achievement_achievements__achievement_id__get`
- **Purpose**: Retrieve a specific achievement by ID
- **Path Parameter**: `achievement_id` (string)
- **Response**: `AchievementSchema` (200 OK)
- **Authentication**: Not required (public endpoint)

#### Packs API

**Tag**: Packs

**POST /packs**
- **Operation ID**: `create_pack_packs_post`
- **Purpose**: Create a new achievement pack
- **Request Body**: `AchievementPackCreateBody`
  ```json
  {
    "name": "string",
    "achievementIds": ["id1", "id2"],
    "previewImageUrl": "string (optional)"
  }
  ```
- **Response**: `AchievementPackSchema` (201 Created)
- **Authentication**: Required

**GET /packs/{pack_code}**
- **Operation ID**: `get_pack_packs__pack_code__get`
- **Purpose**: Get achievement pack by shareable code
- **Path Parameter**: `pack_code` (string)
- **Response**: `AchievementPackSchema` (200 OK)
- **Authentication**: Not required (public endpoint)

**GET /packs-by-id/{pack_id}**
- **Operation ID**: `get_pack_by_id_packs_by_id__pack_id__get`
- **Purpose**: Get achievement pack by ID
- **Path Parameter**: `pack_id` (string)
- **Response**: `AchievementPackSchema` (200 OK)
- **Authentication**: Not required (public endpoint)

#### Users API

**Tag**: Users

**POST /login/token**
- **Operation ID**: `login_token_post`
- **Purpose**: User authentication
- **Request Body**: Form data with username and password
- **Response**: Access token
- **Authentication**: Not required (this endpoint provides tokens)

### Security

**Authentication Scheme**: OAuth2PasswordBearer

- Token-based authentication
- Bearer token in Authorization header
- Required for: Create operations, Upload operations
- Not required for: Read operations (GET endpoints)

## Code Generation

### OpenAPI Generator Plugin

**Plugin**: `org.openapi.generator` version 7.14.0

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

**Generator Settings**:
- **generatorName**: `kotlin` - Generates Kotlin code
- **library**: `multiplatform` - Kotlin Multiplatform support
- **useCoroutines**: `true` - Async operations with coroutines
- **dateLibrary**: `kotlinx-datetime` - KMP-compatible date/time
- **sourceFolder**: `src/commonMain/kotlin` - Shared code location

### Generated Code Location

**Output Directory**: `shared/build/generated/openapi/`

**Package**: `com.plezha.achi.shared.data.network`

**Source Integration**:
```kotlin
kotlin.srcDir("${buildDir}/generated/openapi/src/commonMain/kotlin")
```

Generated code is added to the `commonMain` source set.

### Generated Artifacts

**APIs** (`data/network/apis/`):
- `AchievementsApi.kt`
- `PacksApi.kt`
- `UploadApi.kt`
- `UsersApi.kt`

**Models** (`data/network/models/`):
- `AchievementSchema.kt`
- `AchievementCreateBody.kt`
- `AchievementStepSchema.kt`
- `AchievementStepCreate.kt`
- `AchievementPackSchema.kt`
- `AchievementPackCreateBody.kt`
- `StepProgress.kt`
- Error models, etc.

**Infrastructure** (`data/network/infrastructure/`):
- `ApiClient.kt`
- `HttpResponse.kt`
- Serialization adapters
- Request/response handling

### Build Integration

Generated code is created before Kotlin compilation:

```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}
```

**Process**:
1. Gradle reads `Achi_openapi.json`
2. OpenAPI Generator creates Kotlin code
3. Code placed in build directory
4. Added to source set
5. Compiled with the rest of the project

### Generator Compatibility

**Note in build file**:
> This openapigenerator version is poorly compatible with Ktor 3+, some errors might need to be fixed manually

Generated code may require manual fixes for Ktor 3.x compatibility.

## Ktor Client Configuration

### HTTP Client Setup

**Ktor Version**: 3.1.0

**Platform-Specific Engines**:

**Android** (`App.android.kt`):
```kotlin
actual val httpClientEngine: HttpClientEngine
    get() = OkHttp.create()
```
- Uses OkHttp engine
- Native Android HTTP client
- Best performance on Android

**WasmJS** (inferred from dependencies):
```kotlin
// Uses CIO engine (from wasmJsMain dependencies)
```
- CIO engine for web/WASM
- Pure Kotlin implementation
- Works in browser environment

**iOS** (prepared but not active):
- Would use Darwin engine
- Native iOS networking

### Ktor Dependencies

**Common** (`commonMain`):
```kotlin
implementation(libs.ktor.client.core)              // Core client
implementation(libs.ktor.client.content.negotiation) // JSON support
implementation(libs.ktor.serialization.kotlinx.json) // Kotlin serialization
implementation(libs.ktor.client.logging)           // Request/response logging
```

**Android** (`androidMain`):
```kotlin
implementation(libs.ktor.client.okhttp)  // OkHttp engine
```

**WasmJS** (`wasmJsMain`):
```kotlin
implementation(libs.ktor.client.cio)     // CIO engine
```

### Client Instantiation

**Pattern** (from `App.kt`):
```kotlin
@Composable
private fun AchiAppNav() {
    val usersApi = remember { UsersApi(httpClientEngine = httpClientEngine) }
    val achievementsApi = remember { AchievementsApi(httpClientEngine = httpClientEngine) }
    val packsApi = remember { PacksApi(httpClientEngine = httpClientEngine) }
    val uploadApi = remember { UploadApi(httpClientEngine = httpClientEngine) }
    // ...
}
```

**Key Points**:
- APIs instantiated at app level
- `remember` ensures single instances
- Platform-specific engine injected
- Passed to repositories

### API Usage Pattern

**Generated API Interface**:
```kotlin
class AchievementsApi(
    private val httpClientEngine: HttpClientEngine,
    private val baseUrl: String = ApiClient.BASE_URL
) {
    suspend fun getAchievementAchievementsAchievementIdGet(
        achievementId: String
    ): HttpResponse<AchievementSchema>
    
    suspend fun createAchievementAchievementsPost(
        achievementCreateBody: AchievementCreateBody
    ): HttpResponse<AchievementSchema>
}
```

**HttpResponse Wrapper**:
```kotlin
data class HttpResponse<T>(
    val response: io.ktor.client.statement.HttpResponse,
    val body: T?,
    val success: Boolean
)
```

**Response Handling**:
```kotlin
val response = achievementsApi.getAchievementAchievementsAchievementIdGet(id)
response.check()  // Extension function that throws on error
val achievement = response.body()  // Safe to unwrap after check
```

### Authentication

**Token Management**:

APIs have methods to set authentication token:
```kotlin
fun setAccessToken(token: String) {
    // Sets Bearer token in Authorization header
}
```

**Usage Flow** (from commented code in `App.kt`):
```kotlin
// 1. Login
val loginResponse = usersApi.loginTokenPost("username", "password")
val token = loginResponse.body().accessToken

// 2. Set token on APIs that need it
packsApi.setAccessToken(token)
achievementsApi.setAccessToken(token)
uploadApi.setAccessToken(token)

// 3. Make authenticated requests
packsApi.createPackPacksPost(packCreateBody)
```

## Repository Integration

### API Usage in Repositories

**Example** from `AchievementRepository.kt`:

```kotlin
class MockAchievementRepository(
    private val achievementsApi: AchievementsApi,
) : AchievementRepository {
    
    override suspend fun getAchievement(id: String): Achievement {
        try {
            val response = achievementsApi.getAchievementAchievementsAchievementIdGet(id)
            response.check()  // Throws if not successful
            val achievement = response.body().toAchievement()
            return achievement
        } catch (e: Exception) {
            throw e  // TODO: Better error handling
        }
    }
}
```

**Pattern**:
1. Call API suspend function
2. Check response status
3. Transform network model to domain model
4. Return or update internal state
5. Handle errors

### Complex Operations

**Example** from `AchievementPackRepository.kt`:

Creating a pack involves multiple API calls:

```kotlin
override suspend fun createAchievementPack(
    name: String,
    achievements: List<AchievementCreateBody>,
    packPreviewImagePath: Path,
    imageFileName: String,
): String {
    // 1. Create each achievement
    val ids = ConcurrentSet<String>()
    for (achievement in achievements) {
        val response = achievementsApi.createAchievementAchievementsPost(achievement)
        response.check()
        ids.add(response.body().id)
    }
    
    // 2. Upload pack preview image
    val formPart = createFormPart(packPreviewImagePath, imageFileName)
    val uploadResponse = uploadApi.uploadImageUploadImagePost(formPart, "pack-previews")
    uploadResponse.check()
    val packPreviewImageUrl = uploadResponse.body().toString()
    
    // 3. Create pack with achievement IDs and image URL
    val response = packsApi.createPackPacksPost(
        AchievementPackCreateBody(
            name = name,
            achievementIds = ids.toList(),
            previewImageUrl = packPreviewImageUrl
        )
    )
    response.check()
    return response.body().id
}
```

### Error Handling

**Current Approach**:

Extension function for response checking:
```kotlin
fun <T : Any> HttpResponse<T>.check() {
    if (!success) {
        throw Exception("Network error $response")
    }
}
```

**Repository-Level**:
```kotlin
try {
    // API call
} catch (e: Exception) {
    throw e  // TODO: Better error handling
}
```

**Future Improvements**:
- Custom exception types
- Retry logic
- Offline handling
- User-friendly error messages
- Logging and analytics

## File Upload Handling

### FormPart Creation

**Utility Function** (in `AchievementPackRepository.kt`):

```kotlin
fun createFormPart(
    packPreviewImagePath: Path,
    imageFileName: String,
): FormPart<InputProvider> =
    FormPart(
        key = "image",
        value = InputProvider { 
            SystemFileSystem.source(packPreviewImagePath).buffered() 
        },
        headers = Headers.build {
            append(HttpHeaders.ContentDisposition, "filename=$imageFileName")
        }
    )
```

**Dependencies**:
- `kotlinx.io` for file system access
- Multiplatform file handling
- Streaming upload (not loaded into memory)

**Upload Process**:
1. User selects file (via FileKit)
2. Create FormPart with file path
3. Call upload API with FormPart
4. Receive image URL
5. Use URL in pack creation

## Data Transformation

### Network to Domain Mapping

APIs return network models, repositories transform to domain models:

**Mapping Functions** (in repositories):
```kotlin
fun AchievementSchema.toAchievement() = Achievement(
    id = id,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    steps = steps.map { it.toAchievementStep() },
    previewImageUrl = previewImageUrl,
    imageUrl = imageUrl
)
```

**Benefits**:
- Decouple UI from API
- Add computed properties
- Provide sensible defaults
- Version API independently

## Testing

### Mock Repositories

Current approach uses "Mock" naming but makes real API calls:
```kotlin
class MockAchievementRepository(
    private val achievementsApi: AchievementsApi,
) : AchievementRepository
```

**Future**:
- True mock implementations for testing
- In-memory test data
- No network calls in tests

### API Testing

Commented code in Profile screen for manual testing:
- User creation and login
- Token management
- Achievement creation
- Pack creation

## Configuration

### Base URL

Default base URL from OpenAPI spec:
- Production: ngrok tunnel
- Development: localhost:8000

**Changing Base URL**:
```kotlin
val api = AchievementsApi(
    httpClientEngine = httpClientEngine,
    baseUrl = "https://custom-server.com"
)
```

### Timeouts and Retry

Commented configuration in `App.android.kt`:
```kotlin
// Potential configuration:
// engine {
//     config {
//         retryOnConnectionFailure(true)
//         connectTimeout(5, TimeUnit.SECONDS)
//         readTimeout(10, TimeUnit.SECONDS)
//         writeTimeout(10, TimeUnit.SECONDS)
//     }
// }
```

### Logging

Ktor logging plugin available but commented out:
```kotlin
// Logging {
//     logger = object: Logger {
//         override fun log(message: String) {
//             Log.v(TAG, message)
//         }
//     }
//     level = LogLevel.ALL
// }
```

## Dependencies

### Version Catalog

All API-related dependencies in `gradle/libs.versions.toml`:

```toml
[versions]
ktor = "3.1.0"
kotlinxSerializationJson = "1.8.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```

### Plugins

```kotlin
plugins {
    kotlin("plugin.serialization") version "2.1.0"
    id("org.openapi.generator") version "7.14.0"
}
```

## Best Practices

1. **Always check responses**: Use `.check()` extension
2. **Handle errors gracefully**: Catch and transform exceptions
3. **Map to domain models**: Don't expose network models to UI
4. **Cache where appropriate**: Store in StateFlow to reduce API calls
5. **Use suspend functions**: All API calls are suspending
6. **Inject HTTP engine**: Use platform-appropriate engine
7. **Centralize API instances**: Create once, reuse everywhere
8. **Secure tokens**: Don't hardcode, use secure storage (future)

## Future Enhancements

- **Offline support**: Cache responses, sync when online
- **Request queuing**: Queue requests when offline
- **Better error handling**: Custom exceptions, retry strategies
- **Response caching**: HTTP cache headers
- **Request interceptors**: Logging, analytics, debugging
- **Certificate pinning**: Enhanced security
- **GraphQL**: Consider for complex queries
- **WebSocket support**: Real-time updates
- **API versioning**: Handle multiple API versions
- **Mock server**: Local testing without backend

