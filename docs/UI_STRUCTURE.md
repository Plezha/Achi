# UI Structure Documentation

## Overview

The Achi UI is built with Jetpack Compose Multiplatform using Material 3 design principles. The navigation follows a three-tab bottom navigation pattern with nested navigation graphs for each section.

## Navigation Architecture

### Navigation Structure

```
App Level (AchiAppNav)
├── Scaffold
│   ├── BottomNavigationBar
│   ├── SnackbarHost
│   └── NavHost
│       ├── AddTopRoute (nested navigation)
│       ├── AchievementPackListTopRoute (nested navigation)
│       └── ProfileTopRoute (nested navigation)
```

### Navigation Routes

**Type-Safe Routes** using Kotlin Serialization:

```kotlin
// Top-level routes (shown in bottom nav)
@Serializable private object AchievementPackListTopRoute
@Serializable private object AddTopRoute
@Serializable private object ProfileTopRoute

// Leaf routes
@Serializable private object AchievementPackListRoute
@Serializable private object AddRoute
@Serializable private object CreateAchievementPackRoute
@Serializable private object CreateAchievementRoute
@Serializable private object ProfileRoute
@Serializable private data class AchievementRoute(val id: String)
@Serializable private data class AchievementListRoute(val id: String)
```

### Navigation Graphs

**1. Add Navigation** (`addAchievementsNav`)

```
AddTopRoute (top level)
├── AddRoute (start destination)
│   ├── Shows add by code and add manually options
│   └── NavigationEvent.NavigateToCreatePack → CreateAchievementPackRoute
├── CreateAchievementPackRoute
│   └── Form to create new achievement pack
└── CreateAchievementRoute
    └── (Not yet implemented)
```

**2. Achievement List Navigation** (`achievementListNav`)

```
AchievementPackListTopRoute (top level)
├── AchievementPackListRoute (start destination)
│   ├── List of all packs
│   └── Click pack → navigate(AchievementListRoute(packId))
├── AchievementListRoute(packId)
│   ├── List of achievements in pack
│   └── Click achievement → navigate(AchievementRoute(achievementId))
└── AchievementRoute(achievementId)
    └── Achievement details with progress tracking
```

**3. Profile Navigation** (`profileNav`)

```
ProfileTopRoute (top level)
└── ProfileRoute (start destination)
    └── Test button and commented development code
```

### Bottom Navigation Bar

**Implementation**: `BottomNavigationBar` composable

**Top-Level Routes**:
```kotlin
val topLevelRoutes = listOf(
    TopLevelRoute(AddTopRoute, ic_plus, ic_plus_filled_outside, "Add"),
    TopLevelRoute(AchievementPackListTopRoute, ic_list, ic_cup_filled, "Achievements"),
    TopLevelRoute(ProfileTopRoute, ic_profile, ic_profile_filled, "Profile"),
)
```

**Features**:
- Three tabs: Add, Achievements, Profile
- Icons change based on selection (filled when selected)
- State restoration (saves navigation state on tab switch)
- Single top launch mode (reuses existing screens)
- Automatic back stack management

**Start Destination**: `AchievementPackListTopRoute` (Achievements tab)

### Navigation Transitions

**Configured**: No animations (instant transitions)
```kotlin
enterTransition = { EnterTransition.None },
exitTransition = { ExitTransition.None }
```

This provides a snappy, immediate navigation experience.

### Back Navigation

**Methods**:
- `onBackClicked: () -> Unit` parameter passed to screens
- Calls `navController::navigateUp`
- System back button (Android) handled automatically

**Back Stack Example**:
```
[AchievementPackList] → [AchievementList] → [AchievementDetails]
                    ← Back                 ← Back
```

## Screen Components

### Common Components

Location: `shared/src/commonMain/kotlin/com/plezha/achi/shared/ui/common/`

#### TitleBar

**File**: `TitleBar.kt`

Standardized title bar used across screens.

**Parameters**:
- `text: String` - Title text
- `onBackClicked: (() -> Unit)? = null` - Optional back button
- `modifier: Modifier = Modifier`

**Appearance**:
- Title text centered or left-aligned
- Optional back arrow icon on left
- Material 3 styling

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

**Usage**:
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
ui/list/achievmentdetails/
├── AchievementDetailsScreen.kt    # Screen UI
└── AchievementDetailsViewModel.kt # Business logic & state
```

### Reusable UI Patterns

#### Card Pattern

Used for list items (packs and achievements):

```kotlin
@Composable
fun AchievementCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.clickable(onClick = onClick)) {
        Box(/* Preview image */)
        Column(/* Title and subtitle */)
    }
}
```

**Features**:
- 48dp preview image box with rounded corners
- Title in titleMedium typography
- Subtitle in bodyMedium with reduced opacity
- Clickable with ripple effect

#### Progress Indicators

**Linear Progress** (Achievement Details):
- 8dp height
- Animated with 500ms duration
- FastOutSlowInEasing
- Shows percentage label above

**Circular Progress** (Loading states):
- Centered in Box with fillMaxSize
- Used during data loading

**Custom Step Progress** (Not currently used):
- Canvas-based custom drawing
- Shows dots and connecting lines
- Animates progress through steps

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
- Surface: `#F9F9FF`

**Dark Theme**:
- Primary: `#AAC7FF` (Light blue)
- Secondary: `#BEC6DC` (Light gray-blue)
- Tertiary: `#DDBCE0` (Light purple)
- Background: `#111318` (Nearly black)
- Surface: `#111318`

**Color Roles**: Full Material 3 color system including:
- Primary, Secondary, Tertiary (+ containers and on-colors)
- Error colors
- Surface variants (dim, bright, container levels)
- Inverse colors
- Outline colors

### Typography

**File**: `Type.kt`

**Font Families**:

**Display/Heading Font**: Montserrat
- Light, Regular, Medium, SemiBold, Bold
- Used for: Display text, headlines, titles

**Body Font**: Open Sans
- Light, Regular, Medium, SemiBold, Bold
- Used for: Body text, labels

**Typography Scale**:
```kotlin
displayLarge/Medium/Small    → Montserrat Bold
headlineLarge/Medium/Small   → Montserrat SemiBold
titleLarge/Medium/Small      → Montserrat Medium
bodyLarge/Medium/Small       → Open Sans Normal
labelLarge/Medium/Small      → Open Sans Normal (75% opacity)
```

**Font Loading**:
Fonts loaded from resources using Compose Resources:
```kotlin
Font(Res.font.Montserrat_Bold, weight = FontWeight.Bold)
```

### Theme Configuration

**File**: `Theme.kt`

**Main Theme Composable**:
```kotlin
@Composable
fun AchiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

**Parameters**:
- `darkTheme`: Automatically detects system preference
- `dynamicColor`: Enables Material You dynamic colors (platform-specific)

**Platform-Specific Color Schemes**:

```kotlin
@Composable
expect fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```

**Android** (`Theme.android.kt`):
- Supports Material You dynamic colors (Android 12+)
- Falls back to static colors on older versions

**WasmJS** (`Theme.wasmJs.kt`):
- Uses static color schemes only
- No dynamic color support

### Material 3 Configuration

**Shape System**: Uses Material 3 defaults
- Small components: 8dp rounded corners
- Medium components: 12dp rounded corners
- Large components: 16dp rounded corners

**Elevation System**: Material 3 tonal elevation
- Surface containers at different levels
- Color shifts instead of shadows for depth

## Layout Patterns

### Scaffold Pattern

Main app uses Scaffold for consistent layout:
```kotlin
Scaffold(
    bottomBar = { BottomNavigationBar() },
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { padding ->
    NavHost(modifier = Modifier.padding(padding)) { /*...*/ }
}
```

**Benefits**:
- Automatic padding for system UI
- Consistent placement of navigation and snackbars
- Material 3 layout structure

### Column + LazyColumn Pattern

Most screens follow this pattern:
```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    TitleBar(/*...*/)
    LazyColumn {
        items(listItems) { item ->
            ItemCard(item)
        }
    }
}
```

### Detail Screen Pattern

Achievement details uses advanced layout:
```kotlin
Column(Modifier.fillMaxSize()) {
    TitleBar()
    LazyColumn {
        item { HeroImage() }
        item { ProgressSection() }
        item { Details() }
        item { StepsHeader() }
        items(steps) { StepRow() }
    }
}
```

## Responsive Design

### Image Aspect Ratios

**Achievement Preview**: 48dp square
**Achievement Hero**: 16:9 aspect ratio (1.7:1)
```kotlin
Modifier.aspectRatio(1.7f)
```

### Padding System

**Screen Padding**:
- Horizontal: 16dp
- Vertical: 12dp (list items) / 16dp (sections)

**Component Padding**:
- Internal spacing: 8dp, 12dp, 16dp
- Large section breaks: 32dp

### Text Overflow

Long text handled gracefully:
```kotlin
Text(
    text = subtitle,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1
)
```

## Animation

### Progress Animations

**Configuration**:
```kotlin
animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
)
```

**Applied To**:
- Linear progress bars
- Custom step progress indicators
- State changes feel smooth and responsive

### Visibility Animations

**Loading Overlay** (Add Achievement Screen):
```kotlin
AnimatedVisibility(
    visible = isLoading,
    enter = fadeIn(),
    exit = fadeOut()
) {
    CircularProgressIndicator()
}
```

## State Management in UI

### collectAsState Pattern

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

Automatically recomposes when state changes.

### LaunchedEffect for Events

```kotlin
LaunchedEffect(Unit) {
    viewModel.messageFlow.collectLatest { message ->
        snackbarHostState.showSnackbar(message)
    }
}
```

### Remember for Computed Values

```kotlin
val modifier = remember {
    Modifier.fillMaxWidth().padding(16.dp)
}
```

Prevents unnecessary recomputation.

## Compose Resources

### Resource Types

**Drawables**: Vector icons (XML)
- `ic_arrow_back.xml`
- `ic_cup_filled.xml`, `ic_list.xml`
- `ic_plus.xml`, `ic_plus_filled_outside.xml`
- `ic_profile.xml`, `ic_profile_filled.xml`
- `img.png` (placeholder image)

**Fonts**: TTF files
- Montserrat family (5 weights)
- OpenSans family (5 weights)

**Access Pattern**:
```kotlin
import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_profile

Icon(imageVector = vectorResource(Res.drawable.ic_profile))
Text(fontFamily = FontFamily(Font(Res.font.Montserrat_Bold)))
```

## Platform-Specific UI

### Android-Specific

**AndroidManifest.xml**:
- App name, icon, theme
- Permissions
- Activity configuration

**Theme** (`themes.xml`):
- Splash screen configuration
- Status bar appearance
- Material theme inheritance

**MainActivity**:
- Single activity
- Sets content to `AchiApp()`
- Edge-to-edge display

### WasmJS-Specific

**index.html**:
- HTML container for Compose
- Loads compiled WASM module
- CSS styling

**main.kt**:
- Entry point
- Initializes Compose for Web
- Renders `AchiApp()`

## Accessibility

### Current Implementation

- **Content Descriptions**: Icons have null descriptions (decorative)
- **Semantic Structure**: Proper use of text styles and headings
- **Touch Targets**: Material 3 minimum sizes (48dp)
- **Contrast**: Material 3 color system ensures sufficient contrast

### Future Improvements

- Explicit content descriptions for images
- Screen reader optimization
- Custom semantic properties
- Keyboard navigation enhancements

## Preview System

Compose Multiplatform previews for rapid development:

```kotlin
@Preview
@Composable
private fun AchievementCardPreview() {
    PreviewWrapper {
        AchievementCard(
            achievementExample.title,
            achievementExample.shortDescription,
            {}
        )
    }
}
```

**Benefits**:
- Instant visual feedback
- Test different states
- Preview typography scale
- Verify theme colors

## Performance Optimizations

### Lazy Loading

All lists use `LazyColumn` for efficient rendering:
- Only visible items composed
- Automatic recycling
- Smooth scrolling

### State Management

- `remember` prevents recomputation
- `derivedStateOf` for computed values
- Minimal recomposition scope

### Key Usage

```kotlin
items(items = achievements, key = { it.id }) { /*...*/ }
```

Ensures efficient list updates and animations.

## Future UI Enhancements

- **Pull-to-refresh**: Update pack/achievement lists
- **Swipe actions**: Delete, edit, share
- **Empty states**: Friendly messages when no data
- **Error states**: Retry mechanisms for failed loads
- **Shimmer loading**: Skeleton screens during load
- **Bottom sheets**: Additional actions and filters
- **Dialogs**: Confirmations, forms
- **Animations**: Shared element transitions
- **Dark mode toggle**: Manual override
- **Compact mode**: Denser list layouts

