# Architecture Documentation

## Overview

Achi is a Kotlin Multiplatform (KMP) achievement tracking application built with Compose Multiplatform. The application follows the MVVM (Model-View-ViewModel) architecture pattern with a repository layer for data management and a manual DI container.

## Multiplatform Architecture

### Project Structure

The project uses a single `shared` module that contains all common code:

```
shared/
├── src/
│   ├── commonMain/        # Shared code for all platforms
│   ├── androidMain/       # Android-specific implementations
│   ├── wasmJsMain/        # Web/WasmJS-specific implementations
│   └── iosMain/           # iOS-specific implementations (prepared but not active)
```

### Platform Targets

Currently supported platforms:
- **Android**: Native Android application using Compose for Android
- **WasmJS**: Web application compiled to WebAssembly

Prepared but not active:
- **iOS**: Framework configuration exists but targets are commented out in `build.gradle.kts`

### Platform-Specific Implementations

Platform-specific code is implemented using the `expect`/`actual` pattern:

**HTTP Client Engine** (`App.kt`):
```kotlin
expect val httpClientEngine: HttpClientEngine
```

- **Android** (`App.android.kt`): Uses OkHttp engine
- **WasmJS** (`App.wasmJs.kt`): Uses CIO engine

**Color Scheme** (`Theme.kt`):
```kotlin
@Composable
expect fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```

- **Android**: Supports Material You dynamic colors
- **WasmJS**: Uses static color schemes

## MVVM Architecture Pattern

### Layer Separation

The application follows a clear four-layer architecture:

```
UI Layer (View)
    ↓
ViewModel Layer
    ↓
Repository Layer (Model)
    ↓
Network/Data Source
    ↑
DI Container (AppModule)
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
├── add/                              # Add/create achievements feature
│   ├── AddAchievementScreen.kt
│   ├── AddAchievementsViewModel.kt
│   ├── CreateAchievementPackScreen.kt
│   ├── CreateAchievementPackViewModel.kt
│   ├── EditAchievementScreen.kt
│   └── EditAchievementViewModel.kt
├── list/
│   ├── achievementdetails/           # Achievement details
│   ├── achievementlist/              # List of achievements
│   └── packlist/                     # List of achievement packs
├── profile/                          # User authentication
│   ├── ProfileScreen.kt
│   └── ProfileViewModel.kt
├── common/                           # Shared UI components
└── theme/                            # Theme configuration
```

### ViewModel Layer

Location: Co-located with their views in `ui/` packages

**ViewModels** extend `androidx.lifecycle.ViewModel` and:
- Hold UI state in `StateFlow` or `MutableStateFlow`
- Expose immutable state to the UI
- Handle business logic and user actions
- Interact with repositories
- Use `viewModelScope` for coroutines

Key ViewModels:
- `AchievementPackListViewModel`: Manages pack list state
- `AchievementListViewModel`: Manages achievement list for a specific pack
- `AchievementDetailsViewModel`: Manages individual achievement details with progress
- `AddAchievementsViewModel`: Handles adding packs via code
- `CreateAchievementPackViewModel`: Handles manual pack creation
- `EditAchievementViewModel`: Handles editing achievements during pack creation
- `ProfileViewModel`: Handles authentication UI state

**State Management Pattern**:
```kotlin
class ExampleViewModel(
    private val repository: SomeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // One-time events via Channel
    private val _messageChannel = Channel<String>()
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()
    
    private val _navigationChannel = Channel<NavigationEvent>()
    val navigationFlow: Flow<NavigationEvent> = _navigationChannel.receiveAsFlow()
    
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
- `AchievementPackRepository` / `AchievementPackRepositoryImpl`: Manages achievement packs (CRUD operations)
- `AchievementRepository` / `AchievementRepositoryImpl`: Manages individual achievements
- `UserRepository` / `UserRepositoryImpl`: User's pack collection and progress sync
- `AuthRepository`: Authentication (login/register/logout), token management

**Repository Pattern Benefits**:
- Single source of truth for data
- Testability through interfaces
- Separation of concerns
- Easy to swap implementations

### Dependency Injection

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/di/`

The application uses **manual dependency injection** via `AppModule`:

```kotlin
class AppModule(httpClientEngine: HttpClientEngine) {
    // API Clients (created eagerly)
    val usersApi = UsersApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val authApi = AuthenticationApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val achievementsApi = AchievementsApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val packsApi = PacksApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val uploadApi = UploadApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val userCollectionApi = UserCollectionApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    val userProgressApi = UserProgressApi(baseUrl = ApiConfig.baseUrl, httpClientEngine = httpClientEngine)
    
    // Repositories (created lazily)
    val authRepository: AuthRepository by lazy { ... }
    val userRepository: UserRepository by lazy { ... }
    val achievementRepository: AchievementRepository by lazy { ... }
    val achievementPackRepository: AchievementPackRepository by lazy { ... }
}
```

**Usage in App.kt**:
```kotlin
@Composable
private fun AchiAppNav() {
    val appModule = remember { AppModule(httpClientEngine) }
    // Pass appModule to navigation entry provider
}
```

## Navigation Architecture (Navigation 3)

### Type-Safe Navigation

The app uses **Navigation 3** (`navigation3-ui` library) with Kotlin Serialization for type-safe navigation:

```kotlin
// Routes implement NavKey
@Serializable
data object AchievementPackListRoute : NavKey

@Serializable
data class AchievementRoute(val id: String) : NavKey

// Polymorphic serialization configuration
val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AchievementPackListRoute::class, AchievementPackListRoute.serializer())
            subclass(AchievementRoute::class, AchievementRoute.serializer())
            // ... more routes
        }
    }
}
```

### Navigation Graph Structure

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/navigation/`

Files:
- `Routes.kt`: All route definitions and bottom nav configuration
- `NavGraph.kt`: `createNavEntryProvider()` with all screen entries
- `BottomNavBar.kt`: Bottom navigation component

**Route Hierarchy**:
```
Bottom Navigation
├── Add Tab
│   ├── AddRoute (start)
│   ├── CreateAchievementPackRoute
│   └── EditAchievementRoute(achievementIndex)
├── Achievements Tab (start destination)
│   ├── AchievementPackListRoute (start)
│   ├── AchievementListRoute(packId)
│   └── AchievementRoute(achievementId)
└── Profile Tab
    └── ProfileRoute
```

### Navigation Implementation

**In App.kt**:
```kotlin
@Composable
private fun AchiAppNav() {
    val backStack = rememberNavBackStack(navSavedStateConfig, AchievementPackListRoute)
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = backStack.lastOrNull(),
                onNavigate = { route -> /* handle navigation */ }
            )
        }
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            entryProvider = createNavEntryProvider(
                appModule = appModule,
                backStack = backStack,
                // ...
            )
        )
    }
}
```

**Entry Provider Pattern**:
```kotlin
fun createNavEntryProvider(...) = entryProvider {
    entry<AchievementRoute> { route ->
        val viewModel = remember(route.id) {
            AchievementDetailsViewModel(
                repository = appModule.achievementRepository,
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository
            ).apply {
                loadAchievementById(route.id)
            }
        }
        
        AchievementDetailsScreen(
            viewModel = viewModel,
            onBackClicked = backStack::popBack
        )
    }
}
```

## State Management

### UI State

Each screen has a dedicated UI state data class:
```kotlin
data class PackListUiState(
    val packs: List<AchievementPack> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)
```

### State Flow

State flows from Repository → ViewModel → UI:
1. Repository exposes `StateFlow<Data>`
2. ViewModel collects and transforms to UI state
3. UI observes with `collectAsState()`

### Event Handling

For one-time events (navigation, snackbars), the app uses Channels:
```kotlin
private val _messageChannel = Channel<String>()
val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()
```

Events are collected with `collectLatest` in `LaunchedEffect`.

## Authentication Flow

**AuthRepository** manages:
- Login/registration via API
- Token storage via `multiplatform-settings`
- Token injection to all APIs
- Session restoration on app start

```kotlin
// AuthState exposed via StateFlow
data class AuthState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val accessToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Auth-aware ViewModels** observe auth state to:
- Show login prompts when needed
- Load user-specific data when logged in
- Clear data on logout

## Data Flow Example

Complete flow for tracking achievement progress:

1. **User Action**: User taps +1 on a step
2. **UI**: Calls `viewModel.increaseStepProgress(step, index)`
3. **ViewModel**: 
   - Optimistically updates local UI state
   - Checks if user is logged in
   - If logged in, syncs to server via `userRepository.updateStepProgress()`
4. **UserRepository**: 
   - Calls `userProgressApi.updateStepProgress()`
   - Updates progress cache
5. **UI**: 
   - Already updated from optimistic update
   - Shows error toast if server sync fails

## Threading Model

- **Main/UI Thread**: All Compose UI rendering
- **Coroutines**: Asynchronous operations
  - `viewModelScope`: ViewModel coroutines
  - `Dispatchers.Default`: Background work for CPU-intensive tasks
  - Network operations use Ktor's internal dispatcher

## Key Design Decisions

1. **Single Module**: All code in `shared` module for simplicity
2. **Navigation 3**: Using new KMP navigation library for type-safe multiplatform navigation
3. **Manual DI**: AppModule class instead of DI framework
4. **Optimistic Updates**: UI updates immediately, syncs to server in background
5. **Auth-aware**: Features check login state and prompt when needed
6. **Repository Interfaces**: Abstract implementations for testability
7. **Colocated ViewModels**: ViewModels in same package as their screens

## Future Architecture Considerations

- Consider Koin for DI if complexity grows
- SQLDelight for offline-first architecture
- Use Case layer for complex business logic
- Better error handling with sealed classes
