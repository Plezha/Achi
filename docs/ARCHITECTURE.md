# Architecture Documentation

## Overview

Achi is a Kotlin Multiplatform (KMP) achievement tracking application built with Compose Multiplatform. The application follows the MVVM (Model-View-ViewModel) architecture pattern with a repository layer for data management.

## Multiplatform Architecture

### Project Structure

The project uses a single `shared` module that contains all common code:

```
shared/
├── src/
│   ├── commonMain/        # Shared code for all platforms
│   ├── androidMain/        # Android-specific implementations
│   ├── wasmJsMain/         # Web/WasmJS-specific implementations
│   └── iosMain/            # iOS-specific implementations (prepared but not active)
```

### Platform Targets

Currently supported platforms:
- **Android**: Native Android application using Compose for Android
- **WasmJS**: Web application compiled to WebAssembly

Prepared but not active:
- **iOS**: Framework configuration exists but targets are commented out in `build.gradle.kts`

### Platform-Specific Implementations

Platform-specific code is implemented using the `expect`/`actual` pattern:

**HTTP Client Engine** (`App.kt` line 375):
```kotlin
expect val httpClientEngine: HttpClientEngine
```

- **Android** (`App.android.kt`): Uses OkHttp engine
- **WasmJS** (`App.wasmJs.kt`): Uses CIO engine

**Color Scheme** (`Theme.kt` line 100):
```kotlin
@Composable
expect fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```

- **Android**: Supports Material You dynamic colors
- **WasmJS**: Uses static color schemes

## MVVM Architecture Pattern

### Layer Separation

The application follows a clear three-layer architecture:

```
UI Layer (View)
    ↓
ViewModel Layer
    ↓
Repository Layer (Model)
    ↓
Network/Data Source
```

### UI Layer (View)

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/ui/`

**Screens** are Composable functions that:
- Observe state from ViewModels using `collectAsState()`
- Display UI based on the state
- Handle user interactions by calling ViewModel methods
- Are stateless and recomposable

Example structure:
```
ui/
├── add/                              # Add achievements feature
│   ├── AddAchievementScreen.kt
│   ├── CreateAchievementPackScreen.kt
├── list/
│   ├── achievmentdetails/            # Achievement details
│   ├── achievmentlist/               # List of achievements
│   └── packlist/                     # List of achievement packs
├── common/                           # Shared UI components
└── theme/                            # Theme configuration
```

### ViewModel Layer

Location: Same as UI screens (co-located with their views)

**ViewModels** extend `androidx.lifecycle.ViewModel` and:
- Hold UI state in `StateFlow` or `MutableStateFlow`
- Expose immutable state to the UI
- Handle business logic and user actions
- Interact with repositories
- Use `viewModelScope` for coroutines

Key ViewModels:
- `AchievementPackListViewModel`: Manages pack list state
- `AchievementListViewModel`: Manages achievement list for a specific pack
- `AchievementDetailsViewModel`: Manages individual achievement details
- `AddAchievementsViewModel`: Handles adding packs via code
- `CreateAchievementPackViewModel`: Handles manual pack creation

**State Management Pattern**:
```kotlin
class ExampleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun onUserAction() {
        viewModelScope.launch {
            // Business logic
            _uiState.update { it.copy(newValue = value) }
        }
    }
}
```

### Repository Layer

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/data/`

**Repositories** provide a clean API for data operations:
- Abstract data source details from ViewModels
- Implement business logic related to data
- Handle data transformation between network and domain models
- Manage in-memory caching
- Expose data as `Flow` or suspend functions

Key Repositories:
- `AchievementPackRepository` (Interface) / `AchievementPackRepositoryImpl`: Manages achievement packs
- `AchievementRepository` (Interface) / `MockAchievementRepository`: Manages individual achievements

**Repository Pattern Benefits**:
- Single source of truth for data
- Testability through interfaces
- Separation of concerns
- Easy to swap implementations (mock vs. real)

## Navigation Architecture

### Type-Safe Navigation

The app uses Jetpack Navigation Compose with Kotlin Serialization for type-safe navigation:

```kotlin
@Serializable private object AchievementPackListTopRoute
@Serializable private data class AchievementRoute(val id: String)
```

### Navigation Graph Structure

Three-level navigation hierarchy:

```
Root NavHost
├── AddTopRoute (navigation graph)
│   ├── AddRoute (start)
│   ├── CreateAchievementPackRoute
│   └── CreateAchievementRoute
├── AchievementPackListTopRoute (navigation graph)
│   ├── AchievementPackListRoute (start)
│   ├── AchievementListRoute(packId)
│   └── AchievementRoute(achievementId)
└── ProfileTopRoute (navigation graph)
    └── ProfileRoute (start)
```

**Top-Level Routes** are displayed in the bottom navigation bar:
- Add
- Achievements (start destination)
- Profile

Each top-level route is a nested navigation graph allowing independent navigation stacks.

### Navigation Implementation

Navigation is set up in `App.kt`:
- `AchiAppNav()`: Main composable that sets up the NavHost
- Extension functions on `NavGraphBuilder` define each navigation graph:
  - `addAchievementsNav()`
  - `achievementListNav()`
  - `profileNav()`

## Dependency Injection

The application uses **manual dependency injection** with `remember`:

```kotlin
@Composable
private fun AchiAppNav() {
    val usersApi = remember { UsersApi(httpClientEngine = httpClientEngine) }
    val achievementsApi = remember { AchievementsApi(httpClientEngine = httpClientEngine) }
    val packsApi = remember { PacksApi(httpClientEngine = httpClientEngine) }
    val uploadApi = remember { UploadApi(httpClientEngine = httpClientEngine) }

    val achievementRepository = remember { MockAchievementRepository(achievementsApi) }
    val achievementPackRepository = remember { 
        AchievementPackRepositoryImpl(achievementsApi, packsApi, uploadApi) 
    }
    // ...
}
```

Dependencies are:
1. Created at the app level
2. Stored in Composition using `remember`
3. Passed down to ViewModels
4. ViewModels are created in navigation composables with their dependencies

## State Management

### UI State

Each screen has a dedicated UI state data class:
```kotlin
data class AddAchievementUiState(
    val asciiCode: String = "",
    val isLoading: Boolean = false
)
```

### State Flow

State flows from Repository → ViewModel → UI:
1. Repository exposes `Flow<Data>`
2. ViewModel collects and transforms to UI state
3. UI observes with `collectAsState()`

### Event Handling

For one-time events (like navigation or showing snackbars), the app uses Channels:
```kotlin
private val _messageChannel = Channel<String>()
val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()
```

Events are collected with `collectLatest` in `LaunchedEffect`.

## Data Flow Example

Complete flow for adding an achievement pack by code:

1. **User Action**: User enters code and submits
2. **UI**: Calls `addAchievementViewModel.onCodeSubmit()`
3. **ViewModel**: 
   - Updates loading state
   - Calls `achievementPackRepository.getAchievementPackByCode(code)`
4. **Repository**: 
   - Makes API call via `packsApi`
   - Transforms network model to domain model
   - Updates internal `StateFlow` with new pack
   - Returns result
5. **ViewModel**: 
   - Receives result
   - Sends success/error message via Channel
   - Updates UI state
6. **UI**: 
   - Observes state change
   - Shows snackbar message
   - Clears input field

## Threading Model

- **Main/UI Thread**: All Compose UI rendering
- **Coroutines**: Asynchronous operations
  - `viewModelScope`: ViewModel coroutines (canceled on ViewModel cleared)
  - `Dispatchers.Default`: Background work when specified
  - Network operations use Ktor's internal dispatcher

## Build Configuration

### Gradle Module Structure

```
Project Root
└── shared (Application module)
    - Configured as Android application
    - Contains all multiplatform source sets
```

### Source Set Dependencies

```
commonMain
├── androidMain
├── wasmJsMain
└── iosMain
```

Common dependencies are declared in `commonMain` and platform-specific ones in respective source sets.

## Key Design Decisions

1. **Single Module**: All code in `shared` module for simplicity
2. **MVVM over MVI**: State management with StateFlow rather than strict unidirectional data flow
3. **Manual DI**: No DI framework to reduce complexity and build times
4. **Repository Interfaces**: Abstract implementations for testability
5. **Type-Safe Navigation**: Using Kotlin Serialization for compile-time safety
6. **Colocated ViewModels**: ViewModels in same package as their screens for discoverability

## Future Architecture Considerations

- Consider adding a proper DI framework (Koin) if complexity grows
- Potential migration to MVI for stricter state management
- Use Case layer between ViewModel and Repository for complex business logic
- Offline-first architecture with local database (SQLDelight)

