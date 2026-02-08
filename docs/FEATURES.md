# Features Documentation

## Overview

Achi is an achievement tracking application that allows users to manage collections of achievements (called "packs") and track their progress through various steps. The app features user authentication with server-synced progress. The app is organized around three main sections accessible via bottom navigation.

## Core Features

### 1. Achievement Pack List

**Purpose**: Browse and view all achievement packs in the user's collection.

**Location**: Main "Achievements" tab (center bottom navigation)

**Screen**: `AchievementPackListScreen.kt`

**ViewModel**: `AchievementPackListViewModel.kt`

**Functionality**:
- Displays all achievement packs in user's collection (requires login)
- Shows pack name and achievement count for each pack
- Card-based UI with preview images
- Tap a pack to view its achievements
- Loading state while fetching from server
- Empty state when logged out or no packs

**UI Components**:
- Title bar: "Achievement Packs"
- LazyColumn of pack cards
- Each card shows:
  - Pack preview image (via Coil AsyncImage)
  - Pack name
  - Achievement count (e.g., "5 achievements")

**User Flow**:
1. User logs in
2. App fetches user's pack collection from server
3. Views list of added packs
4. Taps a pack card
5. Navigates to Achievement List for that pack

**Auth Integration**:
- Observes auth state from AuthRepository
- Loads packs when user logs in
- Clears packs when user logs out

### 2. Achievement List

**Purpose**: View all achievements within a specific achievement pack.

**Screen**: `AchievementListScreen.kt`

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
  - Achievement preview image (via Coil AsyncImage)
  - Achievement title
  - Short description

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
  - **Incremental steps**: +/- buttons to adjust progress
- Optimistic UI updates (immediate visual feedback)
- Server sync when logged in
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

**Incremental Step** (substepsAmount > 1):
```
[Description]                    [-] [X/Y] [+]
```
- User taps +/- to adjust progress
- Shows current substeps completed
- Buttons disabled at min/max

**Progress Sync**:
- If logged in: Progress synced to server via UserProgressApi
- If not logged in: Progress is local-only (lost on restart)
- Optimistic updates: UI updates immediately, server sync in background

### 4. Add Achievements

**Purpose**: Add new achievement packs to the user's collection.

**Location**: "Add" tab (left bottom navigation)

**Screen**: `AddAchievementScreen.kt`

**ViewModel**: `AddAchievementsViewModel.kt`

**Functionality**:
Two methods to add achievement packs:

#### Method 1: Create Achievement Pack
- Button to create custom achievement pack
- Navigates to pack creation form

#### Method 2: Add with Code
- User enters a shareable code
- Adds pack to user's collection (requires login)
- Shows success/error message via Snackbar

**UI Components**:
- Title bar: "Add Achievements"
- "Create Achievement Pack" section with button
- "Add with a Code" section:
  - Text field for code entry
  - Submit on keyboard "Done" action
- Loading indicator overlay

**Auth Check**:
- Adding by code requires login
- Shows "Please log in" message if not authenticated

### 5. Create Achievement Pack

**Purpose**: Create a custom achievement pack with achievements.

**Screen**: `CreateAchievementPackScreen.kt`

**ViewModel**: `CreateAchievementPackViewModel.kt`

**Functionality**:
- Create a named achievement pack
- Add multiple achievements to the pack
- Select pack preview image (via FileKit picker)
- Add/edit/remove achievements
- Save to server with image upload

**UI Components**:
- Title bar: "Create Achievement Pack" with back button
- Scrollable form with:
  - Pack name text field
  - Pack description text field
  - Image picker (shows preview when selected)
  - "Achievements" section with count and Add button
  - List of achievement preview cards (clickable to edit)
  - "Save Achievement Pack" button

**Achievement Cards**:
- Show image, title, description, step count
- Clickable to navigate to EditAchievementScreen
- Remove button to delete from pack

**Validation**:
- Pack name required
- At least one achievement required
- All achievements must have titles
- Preview image required

**Save Flow**:
1. Validate all inputs
2. Upload pack preview image
3. Upload achievement images
4. Create each achievement via API
5. Create pack with achievement IDs
6. Add pack to user's collection
7. Navigate back with success message

### 6. Edit Achievement

**Purpose**: Edit an achievement's details during pack creation.

**Screen**: `EditAchievementScreen.kt`

**ViewModel**: `EditAchievementViewModel.kt`

**Functionality**:
- Edit title, short description, long description
- Select achievement image
- Add/edit/remove steps
- Configure substeps amount for incremental steps
- Save or cancel changes

**UI Components**:
- Title bar: "Edit Achievement" with back button
- Title text field (required)
- Short description text field
- Long description text field
- Image picker section
- Steps section:
  - Add Step button
  - Step cards with description and substeps amount
  - Remove button per step
- Save/Cancel buttons

**Step Configuration**:
- Description text field
- Substeps amount (1-100)
- If substeps = 1: Simple checkbox step
- If substeps > 1: Incremental counter step

### 7. Profile / Authentication

**Purpose**: User login, registration, and profile management.

**Location**: "Profile" tab (right bottom navigation)

**Screen**: `ProfileScreen.kt`

**ViewModel**: `ProfileViewModel.kt`

**Functionality**:
- Login with username/password
- Register new account
- View logged-in profile
- Logout

**UI Components**:

**Logged Out State**:
- Profile avatar placeholder
- "Guest" label with sign-in prompt
- Login form:
  - Username field
  - Password field
  - Display name field (registration only)
- Login/Register button
- Toggle between login/register mode
- Settings gear icon in title bar → navigates to Settings screen

**Logged In State**:
- Profile avatar
- Username display
- Account info card
- Logout button (red)
- Settings gear icon in title bar → navigates to Settings screen

**Auth Flow**:
1. User enters credentials
2. ViewModel calls AuthRepository
3. On success:
   - Token saved to settings
   - Token set on all APIs
   - State updated to logged in
   - Snackbar confirmation
4. On error:
   - Error message displayed
   - Form remains editable

### 8. Settings Screen

**Purpose**: App settings accessible from Profile screen via gear icon in the title bar.

**Location**: `SettingsScreen.kt` — navigated via `SettingsRoute`

**Functionality**:
- "Language" row (disabled, placeholder for future in-app language override)
- "Debug Panel" row (only visible in debug builds, gated by `isDebug`) — navigates to `DebugPanelRoute`

### 9. Debug Panel

**Purpose**: Developer tools for testing and debugging (debug builds only).

**Location**: Accessible from Settings screen via "Debug Panel" row

**Screen**: `DebugPanelScreen.kt`

**Functionality**:
- Change API host at runtime (preset options + custom URL)
- Selected host is **persisted** via `multiplatform-settings` and survives app restarts
- Quick login with debug credentials
- Only visible in debug builds (gated by `isDebug`)

**UI Components**:
- Title bar with back navigation
- Current host display card
- Host selection with radio buttons:
  - Production
  - Localhost (Android Emulator)
  - OpenAPI Default
  - Custom URL (with text input)
- Apply button (triggers AppModule recreation)
- Quick Login button

**Build-Type Gating**:
- `isDebug` is an `expect/actual` property:
  - Android: `BuildConfig.DEBUG`
  - WasmJS: `true` (dev platform)
  - iOS: `Platform.isDebugBinary`
- Debug section in ProfileScreen only renders when `isDebug` is true
- DebugPanelRoute registered in navigation but only reachable from debug UI

**Host Switching**:
- `ApiConfig.baseUrl` backed by `MutableStateFlow`, persisted via `Settings`
- On app start, reads saved host from settings (defaults to PROD if none saved)
- `App.kt` observes `ApiConfig.baseUrlFlow` and keys `AppModule` on it
- Changing host recreates entire DI container (all API clients rebuilt)
- All ViewModels are keyed on `appModule`/`baseUrl` so they recreate with fresh repositories

## Feature States

### Fully Implemented ✅
- Achievement Pack List (with auth)
- Achievement List
- Achievement Details (with progress sync)
- Add with Code (with auth)
- Create Achievement Pack (full flow)
- Edit Achievement (with steps)
- Progress tracking (simple & incremental, server-synced)
- User Authentication (login/register/logout)
- Profile management
- Settings screen (gear icon from Profile title bar)
- Debug Panel (debug builds only: host switching, quick login, via Settings)
- Localization (English + Russian, system language, UiText abstraction)

### Auth Requirements
- Pack list: Requires login to see packs
- Add by code: Requires login
- Progress tracking: Syncs only when logged in
- Create pack: Creates on server, adds to user collection

## Cross-Feature Components

### Localization (i18n)

The app supports English (default) and Russian localization via Compose Multiplatform string resources.

**String resources**:
- `composeResources/values/strings.xml` — English (default)
- `composeResources/values-ru/strings.xml` — Russian translations

**In Composables**: `stringResource(Res.string.key)` / `stringResource(Res.string.key, arg1, arg2)`

**In ViewModels**: `UiText` sealed class (`ui/common/UiText.kt`):
- `UiText.Resource(Res.string.key, args...)` — Localizable string resource
- `UiText.Raw(string)` — Non-translatable raw string (server errors, debug)
- `@Composable asString()` — Resolve in composable context
- `suspend resolve()` — Resolve in coroutine context (e.g. LaunchedEffect)

**Language detection**: Uses system language automatically.

**Not translated**: Debug panel strings, preview-only strings.

**Planned**: In-app language override (Settings placeholder exists, disabled).

### Snackbar System

Used for showing messages across features:
- Success messages (pack added, logged in)
- Error messages (pack not found, login failed)
- Implemented at app level in Scaffold
- ViewModels emit messages via `Channel<UiText>`, resolved in NavGraph LaunchedEffect blocks

### Loading States

Implemented per-feature:
- Pack list: isLoading state with indicator
- Add by code: Full-screen loading overlay
- Create pack: Loading overlay during save
- Auth: Button shows progress indicator

### Image Loading

Using Coil 3 with:
- Ktor network fetcher for remote images
- FileKit platform file support for local images
- AsyncImage composable for all images

### Navigation Events

For complex navigation flows:
```kotlin
sealed class NavigationEvent {
    object NavigateToCreatePack : NavigationEvent()
}

sealed class CreatePackNavigationEvent {
    data object NavigateBack : CreatePackNavigationEvent()
}
```

## Data Persistence

**Server-Synced (requires login)**:
- User's pack collection
- Achievement step progress
- Created packs and achievements

**Local Persistence**:
- Auth token and username (multiplatform-settings)

**No Persistence**:
- In-memory achievement cache
- Form state during pack creation

## Error Handling

### Network Errors
Handled in repositories with `.check()` extension:
```kotlin
val response = api.someCall()
response.check()  // Throws if not successful
```

### User-Facing Errors
- Snackbar messages for recoverable errors
- Inline error text for form validation
- Loading states prevent double-submission

### Auth Errors
- 401: "Please log in" message
- 404: "Not found" for invalid codes
- 409: "Already in collection" for duplicate adds

## Platform-Specific Features

### Android
- Material You dynamic colors
- Native file picker via FileKit
- System back button handling
- Activity lifecycle integration

### Web (WasmJS)
- Static color scheme
- Browser-based file selection
- Web-specific HTTP client (CIO)

## Future Feature Roadmap

1. **In-app Language Override**: Settings already has a disabled "Language" row placeholder
2. **Offline Support**: SQLDelight for local caching
3. **Progress Sync Indicator**: Show when progress is syncing
4. **Pack Sharing**: Improved sharing flow
5. **Search**: Find packs and achievements
6. **Categories**: Organize packs by type
7. **Notifications**: Reminders and encouragement
8. **Statistics**: Progress insights
9. **iOS**: Enable iOS platform support
