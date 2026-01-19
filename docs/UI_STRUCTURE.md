# UI Structure Documentation

## Overview

The Achi UI is built with Jetpack Compose Multiplatform using Material 3 design principles. Navigation uses the new Navigation 3 library with a three-tab bottom navigation pattern.

## Navigation Architecture

### Navigation 3 Setup

The app uses **Navigation 3** (`navigation3-ui` library) for type-safe multiplatform navigation:

```kotlin
// In App.kt
val backStack = rememberNavBackStack(navSavedStateConfig, AchievementPackListRoute)

NavDisplay(
    backStack = backStack,
    entryProvider = createNavEntryProvider(...)
)
```

### Navigation Routes

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/navigation/Routes.kt`

All routes implement `NavKey` with Kotlin Serialization:

```kotlin
@Serializable
data object AchievementPackListRoute : NavKey

@Serializable
data object AddRoute : NavKey

@Serializable
data object CreateAchievementPackRoute : NavKey

@Serializable
data class EditAchievementRoute(val achievementIndex: Int) : NavKey

@Serializable
data object ProfileRoute : NavKey

@Serializable
data class AchievementRoute(val id: String) : NavKey

@Serializable
data class AchievementListRoute(val id: String) : NavKey
```

**Polymorphic Serialization** for saved state:
```kotlin
val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AchievementPackListRoute::class, ...)
            // ... all routes
        }
    }
}
```

### Navigation Graph

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/navigation/NavGraph.kt`

**Entry Provider Pattern**:
```kotlin
fun createNavEntryProvider(
    appModule: AppModule,
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState,
    addAchievementsViewModel: AddAchievementsViewModel,
    createAchievementPackViewModel: CreateAchievementPackViewModel
) = entryProvider {
    entry<AchievementRoute> { route ->
        val viewModel = remember(route.id) {
            AchievementDetailsViewModel(...)
        }
        AchievementDetailsScreen(viewModel = viewModel, ...)
    }
    // ... more entries
}
```

### Navigation Helpers

```kotlin
private fun NavBackStack<NavKey>.popBack() {
    if (size > 1) {
        removeAt(size - 1)
    }
}
```

### Bottom Navigation Bar

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/navigation/BottomNavBar.kt`

**Top-Level Routes**:
```kotlin
val topLevelRoutes = listOf(
    TopLevelRoute(AddRoute, ic_plus, ic_plus_filled_outside, "Add"),
    TopLevelRoute(AchievementPackListRoute, ic_list, ic_cup_filled, "Achievements"),
    TopLevelRoute(ProfileRoute, ic_profile, ic_profile_filled, "Profile"),
)
```

**Route Selection Logic**:
```kotlin
val isSelected = when (currentRoute) {
    is AddRoute, is CreateAchievementPackRoute, is EditAchievementRoute -> 
        item.route is AddRoute
    is AchievementPackListRoute, is AchievementListRoute, is AchievementRoute -> 
        item.route is AchievementPackListRoute
    is ProfileRoute -> 
        item.route is ProfileRoute
    else -> false
}
```

**Start Destination**: `AchievementPackListRoute` (Achievements tab)

## Screen Components

### Common Components

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/ui/common/`

#### TitleBar

**File**: `TitleBar.kt`

**Parameters**:
- `text: String` - Title text
- `onBackClicked: (() -> Unit)? = null` - Optional back button
- `modifier: Modifier = Modifier`

**Usage**:
```kotlin
TitleBar(
    text = "Achievement Details",
    onBackClicked = { navController.navigateUp() },
    modifier = Modifier.fillMaxWidth()
)
```

#### PreviewWrapper

**File**: `PreviewWrapper.kt`

Wraps previews with the app theme for consistent preview rendering.

```kotlin
@Preview
@Composable
fun MyComponentPreview() {
    PreviewWrapper {
        MyComponent()
    }
}
```

### Screen Organization

Each feature has its own package with:
- Screen composable
- ViewModel
- UI state data classes

**Example Structure**:
```
ui/list/achievementdetails/
├── AchievementDetailsScreen.kt
└── AchievementDetailsViewModel.kt
```

### Screen Patterns

#### Two-Layer Screen Pattern
Screens use a two-layer pattern for testability:

```kotlin
// Public composable - takes ViewModel
@Composable
fun CreateAchievementPackScreen(
    createAchievementPackViewModel: CreateAchievementPackViewModel,
    onBackClicked: () -> Unit = {},
    onAchievementClick: (Int) -> Unit = {}
) {
    val uiState by createAchievementPackViewModel.uiState.collectAsState()
    
    CreateAchievementPackScreen(
        uiState = uiState,
        onBackClicked = onBackClicked,
        // ... map ViewModel methods to callbacks
    )
}

// Private composable - pure UI, takes state and callbacks
@Composable
private fun CreateAchievementPackScreen(
    uiState: CreateAchievementPackUiState,
    onBackClicked: () -> Unit,
    onPackNameChanged: (String) -> Unit,
    // ... all callbacks
) {
    // UI implementation
}
```

## Theme System

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/ui/theme/`

### Color Scheme

**File**: `Color.kt`

Material 3 color system with light and dark variants.

**Key Colors**:

**Light Theme**:
- Primary: `#415F91` (Blue)
- Secondary: `#565F71` (Gray-blue)
- Tertiary: `#705575` (Purple)
- Background: `#F9F9FF` (Almost white)

**Dark Theme**:
- Primary: `#AAC7FF` (Light blue)
- Secondary: `#BEC6DC` (Light gray-blue)
- Tertiary: `#DDBCE0` (Light purple)
- Background: `#111318` (Nearly black)

### Typography

**File**: `Type.kt`

**Font Families**:
- **Display/Heading Font**: Montserrat (Light, Regular, Medium, SemiBold, Bold)
- **Body Font**: Open Sans (Light, Regular, Medium, SemiBold, Bold)

**Typography Scale**:
```kotlin
displayLarge/Medium/Small    → Montserrat Bold
headlineLarge/Medium/Small   → Montserrat SemiBold
titleLarge/Medium/Small      → Montserrat Medium
bodyLarge/Medium/Small       → Open Sans Normal
labelLarge/Medium/Small      → Open Sans Normal
```

### Theme Configuration

**File**: `Theme.kt`

```kotlin
@Composable
fun AchiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

**Platform-Specific Color Schemes**:
- **Android**: Supports Material You dynamic colors (Android 12+)
- **WasmJS**: Uses static color schemes only

## Layout Patterns

### Scaffold Pattern

Main app uses Scaffold for consistent layout:
```kotlin
Scaffold(
    bottomBar = {
        BottomNavigationBar(
            currentRoute = backStack.lastOrNull(),
            onNavigate = { route -> /* handle */ }
        )
    },
    snackbarHost = {
        SnackbarHost(snackbarHostState)
    }
) { padding ->
    NavDisplay(
        modifier = Modifier.padding(padding),
        // ...
    )
}
```

### Column + LazyColumn Pattern

Most screens follow this pattern:
```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    TitleBar(...)
    LazyColumn {
        items(listItems) { item ->
            ItemCard(item)
        }
    }
}
```

### Form Screen Pattern

Scrollable forms with sections:
```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    TitleBar(onBackClicked = onBackClicked)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        // Form fields
        OutlinedTextField(...)
        Spacer(modifier = Modifier.height(16.dp))
        // ... more fields
        
        // Action buttons at bottom
        Button(onClick = onSave) { ... }
    }
}
```

## Image Loading

Using **Coil 3** with Ktor network fetcher:

**Setup in App.kt**:
```kotlin
setSingletonImageLoaderFactory { context ->
    ImageLoader.Builder(context)
        .components {
            addPlatformFileSupport() // FileKit local files
            add(KtorNetworkFetcherFactory()) // Network images
        }
        .build()
}
```

**Usage**:
```kotlin
AsyncImage(
    model = imageUrl,  // String URL or PlatformFile
    contentDescription = "...",
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)
```

## File Picking

Using **FileKit** for file selection:

```kotlin
val imagePickerLauncher = rememberFilePickerLauncher(
    type = FileKitType.Image
) { file: PlatformFile? ->
    onImageSelected(file)
}

OutlinedButton(onClick = { imagePickerLauncher.launch() }) {
    Text("Select Image")
}
```

## Animation

### Progress Animations

```kotlin
animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
)
```

### Visibility Animations

```kotlin
AnimatedVisibility(
    visible = isLoading,
    enter = fadeIn(),
    exit = fadeOut()
) {
    CircularProgressIndicator()
}
```

### Content Transitions

```kotlin
AnimatedContent(
    targetState = uiState.authState.isLoggedIn,
    label = "auth_content"
) { isLoggedIn ->
    if (isLoggedIn) {
        LoggedInContent(...)
    } else {
        AuthForm(...)
    }
}
```

## State Management in UI

### collectAsState Pattern

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

### LaunchedEffect for Events

```kotlin
LaunchedEffect(Unit) {
    viewModel.messageFlow.collectLatest { message ->
        snackbarHostState.showSnackbar(message)
    }
}

LaunchedEffect(Unit) {
    viewModel.navigationFlow.collectLatest { event ->
        when (event) {
            is NavigationEvent.NavigateToCreatePack -> {
                backStack.add(CreateAchievementPackRoute)
            }
        }
    }
}
```

### Remember for ViewModels

```kotlin
val viewModel = remember(route.id) {
    AchievementDetailsViewModel(...).apply {
        loadAchievementById(route.id)
    }
}
```

## Compose Resources

### Resource Types

**Drawables**: Vector icons (XML)
- `ic_arrow_back.xml`
- `ic_cup_filled.xml`, `ic_list.xml`
- `ic_plus.xml`, `ic_plus_filled_outside.xml`
- `ic_profile.xml`, `ic_profile_filled.xml`

**Fonts**: TTF files
- Montserrat family (5 weights)
- OpenSans family (5 weights)

**Access Pattern**:
```kotlin
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_profile

Icon(imageVector = vectorResource(Res.drawable.ic_profile))
```

## Accessibility

### Current Implementation
- Content descriptions on interactive elements
- Semantic structure (text styles, headings)
- Touch targets meet Material 3 minimums (48dp)
- Contrast via Material 3 color system

## Performance Optimizations

### Lazy Loading
All lists use `LazyColumn`:
- Only visible items composed
- Automatic recycling
- Smooth scrolling

### State Management
- `remember` prevents recomputation
- Key usage in lazy lists:
  ```kotlin
  items(items = achievements, key = { it.id }) { ... }
  ```

### Image Loading
- Coil handles caching
- Async loading with placeholders
- Proper content scaling
