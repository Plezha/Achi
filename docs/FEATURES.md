# Features Documentation

## Overview

Achi is an achievement tracking application that allows users to manage collections of achievements (called "packs") and track their progress through various steps. The app is organized around three main sections accessible via bottom navigation.

## Core Features

### 1. Achievement Pack List

**Purpose**: Browse and view all achievement packs that have been added by the user.

**Location**: Main "Achievements" tab (center bottom navigation)

**Screen**: `AchievementPackListScreen.kt`

**ViewModel**: `AchievementPackListViewModel.kt`

**Functionality**:
- Displays all achievement packs in a scrollable list
- Shows pack name and achievement count for each pack
- Card-based UI with preview images
- Tap a pack to view its achievements

**UI Components**:
- Title bar: "Achievement Packs"
- LazyColumn of pack cards
- Each card shows:
  - Pack preview image (or placeholder)
  - Pack name
  - Achievement count (e.g., "5 achievements")

**User Flow**:
1. User opens the app (starts on this screen)
2. Views list of added packs
3. Taps a pack card
4. Navigates to Achievement List for that pack

**Current State**:
- Packs are stored in memory (StateFlow)
- List updates reactively when packs are added
- No persistence (packs lost on app restart)

### 2. Achievement List

**Purpose**: View all achievements within a specific achievement pack.

**Screen**: `AchievementListScreen.kt` (AchievementsScreen composable)

**ViewModel**: `AchievementListViewModel.kt`

**Functionality**:
- Displays achievements for a selected pack
- Shows title and short description for each achievement
- Back button to return to pack list
- Tap an achievement to view details

**UI Components**:
- Title bar with "Achievements" title and back button
- LazyColumn of achievement cards
- Each card shows:
  - Achievement preview image (or placeholder)
  - Achievement title
  - Short description

**User Flow**:
1. User taps pack from Achievement Pack List
2. ViewModel loads achievements by pack ID
3. Achievements displayed in list
4. User taps achievement to view details
5. Back button returns to pack list

**Data Loading**:
```kotlin
achievementListViewModel.loadAchievementsByPackId(packId)
```

### 3. Achievement Details

**Purpose**: View detailed information about an achievement and track progress through its steps.

**Screen**: `AchievementDetailsScreen.kt`

**ViewModel**: `AchievementDetailsViewModel.kt`

**Functionality**:
- Display achievement details with full image
- Show overall progress (percentage and progress bar)
- List all steps with individual tracking
- Two types of step interaction:
  - **Simple steps**: Checkbox to mark complete/incomplete
  - **Incremental steps**: "+1" button to increment progress
- Animated progress updates
- Back button to return to achievement list

**UI Components**:
- Title bar with "Achievement Details" and back button
- Full-width achievement image (16:9 aspect ratio)
- Progress section:
  - "Progress" label with percentage
  - Animated linear progress bar
- Achievement details:
  - Title (large, bold)
  - Long description (or short description if no long one)
- Steps section:
  - "Steps" header
  - List of steps with appropriate controls

**Step Types**:

**Simple Step** (substepsAmount = 1):
```
[Description]                    [Checkbox]
```
- User checks/unchecks to mark done/undone
- Example: "Первая лаба принята"

**Incremental Step** (substepsAmount > 1):
```
[Description]                    [+1 Button]
Current: X/Y
```
- User taps +1 to increment progress
- Shows current substeps completed
- Button disabled when fully completed
- Example: "Подготовиться хоть чуть чуть к экзу" (0/10)

**Progress Calculation**:
- Overall progress = average of all step progress
- Step progress = substepsDone / substepsAmount
- Animated progress bar smoothly transitions

**User Flow**:
1. User taps achievement from list
2. ViewModel loads achievement by ID
3. Shows loading indicator while loading
4. Displays achievement details and steps
5. User interacts with steps:
   - Check/uncheck simple steps
   - Increment progress on incremental steps
6. Progress bar updates in real-time
7. Back button returns to achievement list

**State Management**:
```kotlin
viewModel.increaseStepProgress(step)  // +1 button
viewModel.setStepCompleted(step, true)  // Check
viewModel.setStepCompleted(step, false)  // Uncheck
```

### 4. Add Achievements

**Purpose**: Add new achievement packs to the collection.

**Location**: "Add" tab (left bottom navigation)

**Screen**: `AddAchievementScreen.kt`

**ViewModel**: `AddAchievementsViewModel.kt`

**Functionality**:
Two methods to add achievement packs:

#### Method 1: Add with Code

- User enters a shareable code
- Fetches pack from server
- Adds to local collection
- Shows success/error message via Snackbar

**UI Components**:
- Section header: "Add with a Code"
- Description text
- Text field for code entry
- Submit on keyboard "Done" action
- Loading indicator overlay

**User Flow**:
1. User taps Add tab
2. Enters achievement pack code in text field
3. Presses keyboard "Done" or navigates away
4. ViewModel calls `onCodeSubmit()`
5. Loading state shown
6. API fetches pack by code
7. Success: 
   - Pack added to repository
   - Snackbar: "{Pack Name} pack successfully added"
   - Input field cleared
8. Error scenarios:
   - Pack already added: "Pack already in the list"
   - Pack not found: "No achievement pack with code X exists"

**Code Flow**:
```kotlin
fun onCodeSubmit() {
    val pack = achievementPackRepository.getAchievementPackByCode(code)
    // Success or error handling
}
```

#### Method 2: Add Manually

- Button to create custom achievement pack
- Navigates to pack creation form

**UI Components**:
- Section header: "Add Manually"
- Description text
- "Add Achievement Pack" button

**User Flow**:
1. User taps "Add Achievement Pack" button
2. ViewModel sends navigation event
3. Navigates to Create Achievement Pack screen

### 5. Create Achievement Pack

**Purpose**: Manually create a custom achievement pack with achievements.

**Screen**: `CreateAchievementPackScreen.kt`

**ViewModel**: `CreateAchievementPackViewModel.kt`

**Functionality**:
- Create a named achievement pack
- Add multiple achievements to the pack
- Set pack description
- Upload pack preview image
- Save to server

**UI Components**:
- Title bar: "Create Achievement Pack"
- Scrollable form with:
  - Pack name text field
  - Pack description text field
  - "Add Achievement" button
  - Dynamic list of achievement forms (one per added achievement):
    - Achievement title field
    - Achievement description field
  - "Save Achievement Pack" button at bottom

**Current Implementation Status**:
- ✅ UI fully implemented
- ✅ Form state management
- ✅ Dynamic achievement list
- ⚠️ File picker integrated but handler incomplete
- ⏳ Save functionality in ViewModel (not fully wired)

**ViewModel State**:
```kotlin
data class CreateAchievementPackUiState(
    val packName: String = "",
    val packDescription: String = "",
    val achievements: List<AchievementFormData> = emptyList()
)
```

**User Flow**:
1. User navigates from Add screen
2. Enters pack name and description
3. Taps "Add Achievement" to add achievement forms
4. Fills in each achievement's title and description
5. (Future) Selects pack preview image
6. Taps "Save Achievement Pack"
7. (Future) Uploads to server and returns to main screen

**Repository Method**:
```kotlin
suspend fun createAchievementPack(
    name: String,
    achievements: List<AchievementCreateBody>,
    packPreviewImagePath: Path,
    imageFileName: String,
): String  // Returns pack ID
```

### 6. Profile / Developer Features

**Purpose**: Testing and development utilities.

**Location**: "Profile" tab (right bottom navigation)

**Screen**: Defined inline in `App.kt` `profileNav()`

**Functionality**:
- Currently: Single test button
- Contains commented-out code for:
  - User authentication testing
  - API testing
  - Pack creation testing

**Current State**: Placeholder for future features

**Potential Future Features**:
- User profile management
- Authentication/login
- Settings
- Statistics/analytics
- Export/import data
- About/help information

## Feature States

### Fully Implemented ✅
- Achievement Pack List
- Achievement List
- Achievement Details
- Add with Code
- Progress tracking (simple & incremental steps)

### Partially Implemented ⚠️
- Create Achievement Pack (UI complete, save not fully connected)

### Planned/Future 🔮
- User authentication
- Profile management
- Offline support with local persistence
- Achievement sharing
- Progress synchronization
- Notifications/reminders
- Achievement categories/tags
- Search and filtering
- Statistics and insights

## Cross-Feature Components

### Snackbar System

Used for showing messages across features:
- Success messages (pack added)
- Error messages (pack not found, already added)
- Implemented at app level in `Scaffold`
- ViewModels emit messages via `Channel<String>`

**Usage**:
```kotlin
LaunchedEffect(Unit) {
    viewModel.messageFlow.collectLatest { message ->
        snackbarHostState.showSnackbar(message)
    }
}
```

### Loading States

Implemented per-feature:
- **Add Achievements**: Full-screen loading overlay with CircularProgressIndicator
- **Achievement Details**: Loading state before showing content
- Prevents user interaction during async operations

### Navigation Events

Used for complex navigation flows:
```kotlin
sealed class NavigationEvent {
    object NavigateToCreatePack : NavigationEvent()
    object NavigateToCreateAchievement : NavigationEvent()
}
```

Collected in UI and trigger navigation:
```kotlin
LaunchedEffect(Unit) {
    viewModel.navigationFlow.collectLatest { event ->
        when (event) {
            NavigateToCreatePack -> navController.navigate(...)
        }
    }
}
```

## Data Persistence

**Current State**: In-memory only
- Packs stored in `StateFlow` in repository
- Lost on app restart

**Impact**:
- Users must re-add packs each session
- Progress tracking not persisted

**Future**: 
- Local database (SQLDelight)
- Achievement and pack caching
- Progress persistence
- Offline-first architecture

## Error Handling

### Network Errors

Handled in repositories:
```kotlin
try {
    val response = api.getAchievement(id)
    response.check()  // Throws if not successful
    // Process response
} catch (e: Exception) {
    throw e  // TODO: Better error handling
}
```

### User-Facing Errors

Communicated via:
- Snackbar messages for recoverable errors
- Error states in UI (future)
- Toast messages (platform-specific, future)

### Validation Errors

- Pack already exists: IllegalStateException
- Pack not found: NoSuchElementException
- Invalid step progress: require() in data class

## Feature Dependencies

```
Achievement Pack List
    ↓
Achievement List (requires pack ID)
    ↓
Achievement Details (requires achievement ID)

Add Achievements
    ↓
Create Achievement Pack (optional path)
```

All features depend on:
- API availability
- Network connection
- Valid authentication (future)

## Accessibility Features

Current implementation:
- Content descriptions on images
- Keyboard navigation support (text fields)
- Semantic structure (headers, lists)

Future improvements:
- Screen reader optimization
- Contrast adjustments
- Font scaling support
- Voice commands
- Haptic feedback

## Performance Considerations

- **Lazy Loading**: LazyColumn for large lists
- **State Hoisting**: Efficient recomposition
- **Remember**: Caching computed values
- **Coroutines**: Non-blocking operations
- **Animated Progress**: Smooth 500ms transitions with easing

## Platform-Specific Features

### Android
- Material You dynamic colors
- Native file picker
- System back button handling
- Activity lifecycle integration

### Web (WasmJS)
- Webpack dev server
- Static color scheme
- Browser-based file selection
- Web-specific HTTP client (CIO)

## Future Feature Roadmap

1. **Persistence Layer**: SQLDelight integration
2. **Authentication**: User login/registration
3. **Sync**: Cloud backup and multi-device sync
4. **Sharing**: Share achievements with friends
5. **Gamification**: Points, levels, badges
6. **Social**: Leaderboards, challenges
7. **Customization**: Themes, achievement templates
8. **Analytics**: Progress insights, statistics
9. **Notifications**: Reminders, encouragement
10. **Export**: PDF reports, data export

