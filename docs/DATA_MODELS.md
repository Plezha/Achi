# Data Models Documentation

## Overview

The Achi application uses a layered data model approach with clear separation between:
- **Domain Models**: Business logic models used throughout the app
- **Network Models**: API response/request models (generated from OpenAPI spec)
- **Mapping Functions**: Transform network models to domain models

## Domain Models

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/model/`

### Achievement

The core model representing an achievement that a user can complete.

```kotlin
data class Achievement(
    val id: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String? = null,
    val steps: List<AchievementStep>,
    val previewImageUrl: String? = null,
    val imageUrl: String? = null,
    val isCompleted: Boolean = false
)
```

**Fields**:
- `id`: Unique identifier (from backend)
- `title`: Main achievement title
- `shortDescription`: Brief description shown in lists
- `longDescription`: Detailed description shown in achievement details (optional)
- `steps`: List of steps required to complete the achievement (can be empty for stepless achievements)
- `previewImageUrl`: Small preview image URL (optional)
- `imageUrl`: Full-size image URL for details screen (optional)
- `isCompleted`: Direct completion flag for stepless achievements (default false)

**Computed Properties**:

```kotlin
val progress: Double
    // For achievements with steps: average progress across all steps (0.0 to 1.0)
    // For stepless achievements: 1.0 if isCompleted, 0.0 otherwise
    
val stepsDone: Int
    // Count of completed steps
    
val isDone: Boolean
    // For achievements with steps: true if all steps completed
    // For stepless achievements: true if isCompleted
```

### AchievementStep

Represents a single step within an achievement.

```kotlin
data class AchievementStep(
    val description: String,
    val progress: StepProgress = StepProgress(),
)
```

**Fields**:
- `description`: What needs to be done
- `progress`: Current progress state (substeps completed)

**Computed Properties**:

```kotlin
val isDone: Boolean
    // True if all substeps are completed
```

**Helper Methods**:

```kotlin
fun asCompleted(): AchievementStep
    // Returns a copy with progress set to completed
    
fun asNotStarted(): AchievementStep
    // Returns a copy with progress reset to 0
```

**Step Types**:
1. **Simple Step**: Single checkbox (substepsAmount = 1)
   - UI: Checkbox that can be checked/unchecked
   
2. **Incremental Step**: Multiple substeps (substepsAmount > 1)
   - UI: +/- buttons with progress counter

### StepProgress

Tracks completion progress for a step.

```kotlin
data class StepProgress(
    val substepsDone: Int = 0,
    val substepsAmount: Int = 1
)
```

**Fields**:
- `substepsDone`: Number of substeps completed (0 to substepsAmount)
- `substepsAmount`: Total number of substeps (minimum 1)

**Validation**:
```kotlin
init {
    require(substepsDone in 0..substepsAmount)
}
```

**Computed Properties**:

```kotlin
val progressFloat: Float
    // Progress as a float (0.0 to 1.0)
    
val isCompleted: Boolean
    // True if substepsDone == substepsAmount
```

**Helper Methods**:

```kotlin
fun asCompleted(): StepProgress
    // Returns copy with substepsDone = substepsAmount
    
fun asNotStarted(): StepProgress
    // Returns copy with substepsDone = 0
```

### AchievementPack

A collection of related achievements that can be shared via a code.

```kotlin
data class AchievementPack(
    val id: String,
    val name: String,
    val count: Int,
    val achievementIds: List<String>,
    val previewImageUrl: String? = null,
    val code: String
)
```

**Fields**:
- `id`: Unique identifier (from backend)
- `name`: Pack name
- `count`: Number of achievements in the pack
- `achievementIds`: List of achievement IDs contained in this pack
- `previewImageUrl`: Preview image for the pack (optional)
- `code`: Shareable code to add this pack

## UI State Models

### Authentication

```kotlin
data class AuthState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val accessToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
```

### Pack Creation

```kotlin
@JvmInline
value class AchievementId(val value: String)

data class EditableStep(
    val description: String = "",
    val substepsAmount: Int = 1
)

data class EditableAchievementData(
    val id: AchievementId,
    val title: String = "",
    val shortDescription: String = "",
    val longDescription: String = "",
    val steps: List<EditableStep> = listOf(),
    val imageFile: PlatformFile? = null
)

data class CreateAchievementPackUiState(
    val packName: String = "",
    val packDescription: String = "",
    val achievements: List<EditableAchievementData> = listOf(),
    val selectedImageFile: PlatformFile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = packName.isNotBlank() && 
                achievements.isNotEmpty() && 
                achievements.all { it.title.isNotBlank() } &&
                selectedImageFile != null &&
                !isLoading
}
```

## Network Models

Location: `shared/build/generated/openapi/src/commonMain/kotlin/.../data/network/models/`

These models are **auto-generated** from `Achi_openapi.json`.

### Key Response Models

**AchievementSchema**:
```kotlin
data class AchievementSchema(
    val id: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String?,
    val steps: List<AchievementStepSchema>,
    val previewImageUrl: String?,
    val imageUrl: String?
)
```

**AchievementStepSchema**:
```kotlin
data class AchievementStepSchema(
    val description: String,
    val substepsAmount: Int?
)
```

**AchievementPackSchema**:
```kotlin
data class AchievementPackSchema(
    val id: String,
    val name: String,
    val count: Int,
    val achievementIds: List<String>,
    val previewImageUrl: String?,
    val code: String
)
```

**UserAchievementProgress**:
```kotlin
data class UserAchievementProgress(
    val achievementId: String,
    val steps: List<StepProgress>
)
```

### Request Models

**AchievementCreateBody**:
```kotlin
data class AchievementCreateBody(
    val title: String,
    val shortDescription: String,
    val steps: List<AchievementStepCreate>,
    val longDescription: String? = null,
    val previewImageUrl: String? = null,
    val imageUrl: String? = null
)
```

**AchievementStepCreate**:
```kotlin
data class AchievementStepCreate(
    val description: String,
    val substepsAmount: Int? = null
)
```

**AchievementPackCreateBody**:
```kotlin
data class AchievementPackCreateBody(
    val name: String,
    val achievementIds: List<String>,
    val previewImageUrl: String? = null
)
```

**StepProgressUpdateBody**:
```kotlin
data class StepProgressUpdateBody(
    val substepsDone: Int
)
```

## Model Mapping

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/network/Mappers.kt`

### Network to Domain Mapping

```kotlin
// Resolves relative URLs by prepending base URL
private fun resolveImageUrl(url: String?): String? {
    if (url == null) return null
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "${ApiConfig.baseUrl}$url"
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
```

### Progress Mapping (UserRepository)

```kotlin
fun UserAchievementProgress.toStepProgressList(): List<StepProgress> {
    return steps.map { serverStep ->
        StepProgress(
            substepsDone = serverStep.substepsDone ?: 0,
            substepsAmount = serverStep.substepsAmount ?: 1
        )
    }
}
```

## Data Flow Through Layers

### Reading Data (Example: Load Achievement with Progress)

```
1. UI calls ViewModel.loadAchievementById(id)
   ↓
2. ViewModel calls repository.getAchievement(id)
   ↓
3. Repository checks in-memory cache
   ↓
4. If not cached: calls achievementsApi
   ↓
5. Receives AchievementSchema (network model)
   ↓
6. Maps to Achievement (domain model)
   ↓
7. If logged in: loads progress from UserRepository
   ↓
8. Merges progress with achievement steps
   ↓
9. Returns Achievement to ViewModel
   ↓
10. ViewModel updates UI state
   ↓
11. UI observes state and displays Achievement
```

### Creating Data (Example: Create Pack)

```
1. UI calls ViewModel.onSaveAchievementPack()
   ↓
2. ViewModel collects form data
   ↓
3. Converts EditableAchievementData to AchievementCreateBody list
   ↓
4. Calls repository.createAchievementPack(...)
   ↓
5. Repository:
   - Uploads achievement images → gets URLs
   - Creates each achievement via API → gets IDs
   - Uploads pack preview image → gets URL
   - Creates pack with IDs and URL
   ↓
6. Adds pack to user's collection
   ↓
7. Returns created pack to ViewModel
   ↓
8. ViewModel navigates back with success message
```

## Model Immutability

All domain models are **immutable `data class`**:
- Thread-safe
- Predictable state
- Easy to test
- Works well with Compose recomposition

To modify: create a new instance using `copy()`:
```kotlin
val updatedStep = step.copy(progress = StepProgress(5, 10))
```

## Extension Functions

**Achievement with Progress**:
```kotlin
fun Achievement.withProgress(progressList: List<StepProgress>, isCompleted: Boolean = false): Achievement {
    return copy(
        isCompleted = isCompleted,
        steps = if (progressList.size != steps.size) steps else {
            steps.mapIndexed { index, step ->
                step.copy(progress = progressList[index])
            }
        }
    )
}
```

**Step Progress Updates**:
```kotlin
fun AchievementStep.withProgressIncreased() =
    copy(progress = progress.copy(
        substepsDone = (progress.substepsDone + 1).coerceAtMost(progress.substepsAmount)
    ))

fun AchievementStep.withProgressDecreased() =
    copy(progress = progress.copy(
        substepsDone = (progress.substepsDone - 1).coerceAtLeast(0)
    ))
```

## Null Safety

Fields use Kotlin's null safety:
- **Non-null**: `title: String` - must always have a value
- **Nullable**: `longDescription: String?` - optional fields
- **Defaults**: `substepsDone: Int = 0` - default values provided

## Validation

### StepProgress Validation
```kotlin
init {
    require(substepsDone in 0..substepsAmount)
}
```

### UI State Validation
```kotlin
val canSave: Boolean
    get() = packName.isNotBlank() && 
            achievements.isNotEmpty() && 
            achievements.all { it.title.isNotBlank() } &&
            selectedImageFile != null &&
            !isLoading
```
