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
    val imageUrl: String? = null
)
```

**Fields**:
- `id`: Unique identifier (from backend)
- `title`: Main achievement title (e.g., "САПР")
- `shortDescription`: Brief description shown in lists
- `longDescription`: Detailed description shown in achievement details (optional)
- `steps`: List of steps required to complete the achievement
- `previewImageUrl`: Small preview image URL (optional)
- `imageUrl`: Full-size image URL for details screen (optional)

**Computed Properties**:

```kotlin
val progress: Double
    // Average progress across all steps (0.0 to 1.0)
    // Calculated as: sum of all step progress / number of steps
    
val stepsDone: Int
    // Count of completed steps
    
val isDone: Boolean
    // True if all steps are completed
```

**Usage**:
- Displayed in achievement lists
- Shown in detail screens
- Progress tracking
- Completion status

### AchievementStep

Represents a single step within an achievement.

```kotlin
data class AchievementStep(
    val description: String,
    val progress: StepProgress = StepProgress(),
)
```

**Fields**:
- `description`: What needs to be done (e.g., "Первая лаба принята")
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
   - Example: "Первая лаба принята"
   - UI: Checkbox that can be checked/unchecked
   
2. **Incremental Step**: Multiple substeps (substepsAmount > 1)
   - Example: "Подготовиться хоть чуть чуть к экзу" (0/10)
   - UI: "+1" button to increment progress, shows current count

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
    // Calculated as: substepsDone / substepsAmount
    
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
- `name`: Pack name (e.g., "Computer Science Semester 1")
- `count`: Number of achievements in the pack
- `achievementIds`: List of achievement IDs contained in this pack
- `previewImageUrl`: Preview image for the pack (optional)
- `code`: Shareable code to add this pack (e.g., "ABC")

**Usage**:
- Browse available achievement packs
- Add packs by code
- Group related achievements together
- Share achievement collections

## Network Models

Location: `shared/build/generated/openapi/src/commonMain/kotlin/.../data/network/models/`

These models are **auto-generated** from `Achi_openapi.json` using the OpenAPI Generator plugin.

### Key Network Models

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
    val progress: StepProgress?
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

**Request Models**:

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

## Model Mapping

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/`

### Network to Domain Mapping

**Achievement Mapping** (`AchievementRepository.kt`):

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

fun AchievementStepSchema.toAchievementStep() = AchievementStep(
    description = description,
    progress = progress?.toDomainStepProgress() ?: StepProgress(0, 1)
)

fun com.plezha.achi.shared.data.network.models.StepProgress.toDomainStepProgress() = 
    StepProgress(
        substepsDone ?: 0,
        substepsAmount ?: 1
    )
```

**AchievementPack Mapping** (`AchievementPackRepository.kt`):

```kotlin
private fun AchievementPackSchema.toAchievementPack() = AchievementPack(
    id = id,
    name = name,
    count = count,
    achievementIds = achievementIds,
    previewImageUrl = previewImageUrl,
    code = code
)
```

### Mapping Strategy

1. **Receive** network model from API
2. **Transform** to domain model using extension functions
3. **Store** domain model in repository
4. **Expose** domain model to ViewModels

**Benefits**:
- Decouples UI from API changes
- Provides sensible defaults (e.g., `StepProgress(0, 1)`)
- Keeps domain models clean and focused
- Enables offline-first architecture in the future

## Data Validation

### StepProgress Validation

The only domain model with built-in validation:

```kotlin
data class StepProgress(
    val substepsDone: Int = 0,
    val substepsAmount: Int = 1
) {
    init {
        require(substepsDone in 0..substepsAmount)
    }
}
```

This ensures:
- `substepsDone` is never negative
- `substepsDone` never exceeds `substepsAmount`
- Invalid states cannot be created

### Repository-Level Validation

Repositories perform additional validation:

**Example from `AchievementPackRepository.kt`**:
```kotlin
val alreadyAddedPack = currentPacks.find { it.id == newPack.id }
if (alreadyAddedPack != null) {
    throw IllegalStateException(
        "Pack \"${alreadyAddedPack.name}\" is already in the list"
    )
}
```

## Example Data

### Sample Achievement

From `AchievementRepository.kt`:

```kotlin
val achievementExample = Achievement(
    id = "1",
    title = "САПР",
    shortDescription = "Системный Анализ и Принятие Решений",
    steps = listOf(
        AchievementStep(description = "Первая лаба принята"),
        AchievementStep(description = "Вторая лаба принята"),
        AchievementStep(description = "Третья лаба принята"),
        AchievementStep(description = "Четвертая лаба принята"),
        AchievementStep(description = "Пятая лаба принята"),
        AchievementStep(description = "Шестая лаба принята"),
        AchievementStep(
            description = "Подготовиться хоть чуть чуть к экзу",
            progress = StepProgress(0, 10)
        ),
        AchievementStep(description = "В зачётке зачёт"),
    ),
)
```

This example demonstrates:
- Multiple simple steps (labs 1-6)
- One incremental step (exam preparation with 10 substeps)
- A final completion step (grade recorded)

## Data Flow Through Layers

### Reading Data (Example: Load Achievement)

```
1. UI calls ViewModel.loadAchievementById(id)
   ↓
2. ViewModel calls repository.getAchievement(id)
   ↓
3. Repository checks in-memory cache
   ↓
4. If not cached: calls achievementsApi.getAchievementAchievementsAchievementIdGet(id)
   ↓
5. Receives AchievementSchema (network model)
   ↓
6. Maps to Achievement (domain model) using .toAchievement()
   ↓
7. Caches in repository's StateFlow
   ↓
8. Returns Achievement to ViewModel
   ↓
9. ViewModel updates UI state
   ↓
10. UI observes state and displays Achievement
```

### Creating Data (Example: Create Pack)

```
1. UI calls ViewModel.onSaveAchievementPack()
   ↓
2. ViewModel collects form data and transforms to AchievementCreateBody list
   ↓
3. Calls repository.createAchievementPack(name, achievements, imagePath, fileName)
   ↓
4. Repository:
   - Creates each achievement via achievementsApi (receives AchievementSchema with ID)
   - Uploads pack preview image via uploadApi
   - Creates pack with collected IDs via packsApi (AchievementPackCreateBody)
   ↓
5. Returns pack ID to ViewModel
   ↓
6. ViewModel updates UI state (success/error)
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

## Null Safety

Fields use Kotlin's null safety:
- **Non-null**: `title: String` - must always have a value
- **Nullable**: `longDescription: String?` - optional fields
- **Defaults**: `substepsDone: Int = 0` - default values provided

This provides compile-time safety and clear API contracts.

## Future Model Extensions

Potential additions to support new features:
- `userId` in Achievement for multi-user support
- `completedAt: Instant?` for completion timestamps
- `tags: List<String>` for categorization
- `difficulty: Difficulty` enum for achievement difficulty
- `points: Int` for gamification
- Local-only fields for offline support (sync status, etc.)

