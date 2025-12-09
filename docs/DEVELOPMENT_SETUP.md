# Development Setup Guide

## Prerequisites

### Required Software

#### Java Development Kit (JDK)
- **Version**: JDK 11 or higher
- **Recommended**: JDK 17 (LTS)
- **Purpose**: Gradle and Android builds
- **Installation**:
  - macOS: `brew install openjdk@17`
  - Windows: Download from [Adoptium](https://adoptium.net/)
  - Linux: `sudo apt install openjdk-17-jdk`

**Verify Installation**:
```bash
java -version
javac -version
```

#### Gradle
- **Version**: Managed by Gradle Wrapper (included)
- **No separate installation needed**
- Wrapper version specified in `gradle/wrapper/gradle-wrapper.properties`

#### Android SDK (for Android development)
- **Minimum SDK**: 24
- **Target SDK**: 35
- **Compile SDK**: 35
- **Installation**: Via Android Studio or command line tools

#### IDE

**Recommended**: IntelliJ IDEA or Android Studio
- **IntelliJ IDEA**: 2023.3 or later
- **Android Studio**: Hedgehog (2023.1.1) or later

**Required Plugins**:
- Kotlin Multiplatform Mobile (KMM)
- Kotlin
- Compose Multiplatform
- Android support (for Android Studio)

### Optional Software

**For Web Development**:
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Browser DevTools for debugging

**For iOS Development** (prepared but not active):
- macOS required
- Xcode 15 or later
- CocoaPods or Swift Package Manager

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd Achi
```

### 2. Project Structure

```
Achi/
├── gradle/                    # Gradle wrapper and version catalog
├── shared/                    # Main KMP module
│   ├── src/
│   │   ├── androidMain/       # Android-specific code
│   │   ├── commonMain/        # Shared code
│   │   ├── iosMain/           # iOS-specific code
│   │   └── wasmJsMain/        # Web-specific code
│   └── build.gradle.kts       # Module build configuration
├── Achi_openapi.json          # API specification
├── build.gradle.kts           # Root build configuration
├── settings.gradle.kts        # Project settings
└── gradlew / gradlew.bat      # Gradle wrapper scripts
```

### 3. Initial Build

**Generate OpenAPI Code**:
```bash
./gradlew openApiGenerate
```

This generates API client code before first build.

**Build Project**:
```bash
./gradlew build
```

**First build will**:
- Download Gradle dependencies
- Generate OpenAPI client code
- Compile Kotlin code for all targets
- Run tests (if any)
- Create build artifacts

**Expected Duration**: 2-5 minutes (first time)

### 4. Verify Setup

```bash
# Check all tasks
./gradlew tasks

# Clean build
./gradlew clean build

# Check for issues
./gradlew check
```

## Platform-Specific Setup

### Android

#### Android Studio Setup

1. **Open Project**:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to project root
   - Wait for Gradle sync

2. **SDK Configuration**:
   - Tools → SDK Manager
   - Install Android SDK 35
   - Install Android Build Tools 35.x.x

3. **Local Properties** (if needed):
   
   Create `local.properties` in project root:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```
   
   Typical paths:
   - macOS: `/Users/<username>/Library/Android/sdk`
   - Windows: `C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk`
   - Linux: `/home/<username>/Android/Sdk`

#### Run on Android

**Via Android Studio**:
1. Select "shared" module
2. Choose device/emulator
3. Click Run button

**Via Command Line**:
```bash
# Install on connected device
./gradlew installDebug

# Install and run
./gradlew shared:installDebug
adb shell am start -n com.plezha.achi/.MainActivity
```

**Build APK**:
```bash
./gradlew assembleDebug
# Output: shared/build/outputs/apk/debug/shared-debug.apk
```

**Build Release APK**:
```bash
./gradlew assembleRelease
# Note: Requires signing configuration
```

#### Android Emulator

**Create Emulator**:
1. Tools → Device Manager
2. Create Device
3. Select hardware profile (e.g., Pixel 5)
4. Download system image (API 35, Android 15)
5. Finish and launch

**Command Line**:
```bash
# List emulators
emulator -list-avds

# Start emulator
emulator -avd <avd_name>
```

### Web (WasmJS)

#### Prerequisites

- Modern browser with WebAssembly support
- Node.js (for Webpack dev server, installed automatically)

#### Run Web Version

**Development Server**:
```bash
./gradlew wasmJsBrowserDevelopmentRun
```

**What happens**:
1. Compiles Kotlin to WebAssembly
2. Starts Webpack dev server
3. Opens browser automatically
4. Watches for file changes
5. Auto-reloads on changes

**Access**:
- URL: `http://localhost:8080` (or port shown in console)
- Development mode: Includes source maps and debugging

**Production Build**:
```bash
./gradlew wasmJsBrowserProductionWebpack
```

**Output**: `shared/build/dist/wasmJs/productionExecutable/`

**Files**:
- `achi.js` - Main JavaScript file
- `achi.wasm` - WebAssembly binary
- `index.html` - Entry point

#### Serve Production Build

```bash
cd shared/build/dist/wasmJs/productionExecutable
python3 -m http.server 8000
# Or use any static file server
```

Access: `http://localhost:8000`

### iOS (Prepared, Not Active)

**Current Status**: iOS targets commented out in `build.gradle.kts`

**To Enable**:

1. Uncomment in `shared/build.gradle.kts`:
```kotlin
listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
).forEach { iosTarget ->
    iosTarget.binaries.framework {
        baseName = "sharedKit"
        isStatic = true
    }
}
```

2. Build framework:
```bash
./gradlew linkDebugFrameworkIosArm64
```

3. Integrate with Xcode project (requires Xcode project setup)

## IDE Configuration

### IntelliJ IDEA / Android Studio

#### Kotlin Plugin

1. Settings → Plugins
2. Search "Kotlin"
3. Install/Update to latest version
4. Restart IDE

#### Compose Multiplatform Plugin

1. Settings → Plugins
2. Search "Compose Multiplatform"
3. Install
4. Restart IDE

#### Code Style

Project uses default Kotlin code style.

**Optional**: Configure in Settings → Editor → Code Style → Kotlin

#### Run Configurations

**Android**:
- Module: shared
- Target: Device/Emulator
- Activity: MainActivity

**WasmJS**:
- Task: `wasmJsBrowserDevelopmentRun`
- Module: shared

### VS Code (Alternative)

**Plugins**:
- Kotlin Language
- Gradle for Java
- Android iOS Emulator

**Tasks**: Use integrated terminal for Gradle commands

## Environment Variables

### Optional Configuration

**API Base URL** (if different from default):

Currently hardcoded in generated API code. To change:

1. Modify `Achi_openapi.json` servers
2. Regenerate: `./gradlew openApiGenerate`

Or override in code:
```kotlin
val api = AchievementsApi(
    httpClientEngine = httpClientEngine,
    baseUrl = "https://your-server.com"
)
```

### No Environment Variables Required

The app doesn't require environment variables for basic development.

## Common Build Tasks

### Cleaning

```bash
# Clean all builds
./gradlew clean

# Clean specific target
./gradlew cleanAndroidDebug
./gradlew cleanWasmJsBrowserDevelopment
```

### Building

```bash
# Build all targets
./gradlew build

# Build Android only
./gradlew assembleDebug

# Build Web only
./gradlew wasmJsBrowserProductionWebpack
```

### OpenAPI Code Generation

```bash
# Regenerate API code
./gradlew openApiGenerate

# Clean and regenerate
./gradlew clean openApiGenerate
```

**When to regenerate**:
- After updating `Achi_openapi.json`
- When API changes on backend
- After pulling updates with API changes

### Dependencies

```bash
# Show dependency tree
./gradlew dependencies

# Show updates available
./gradlew dependencyUpdates
```

## Running Tests

**Current Status**: Basic test files exist, no comprehensive tests yet.

```bash
# Run all tests
./gradlew test

# Android tests
./gradlew testDebugUnitTest

# Common tests
./gradlew cleanTestCommonMain
```

**Test Locations**:
- Unit tests: `shared/src/commonTest/`
- Android tests: `shared/src/androidHostTest/`
- Instrumented tests: `shared/src/androidDeviceTest/`

## Troubleshooting

### Gradle Sync Issues

**Problem**: "Could not resolve dependencies"

**Solution**:
```bash
./gradlew --refresh-dependencies
./gradlew clean build
```

### OpenAPI Generation Fails

**Problem**: API code generation errors

**Solution**:
1. Verify `Achi_openapi.json` is valid JSON
2. Delete build directory: `rm -rf shared/build/generated`
3. Regenerate: `./gradlew openApiGenerate`

### Android Build Fails

**Problem**: SDK not found

**Solution**:
1. Create/update `local.properties` with SDK path
2. Or set `ANDROID_HOME` environment variable

**Problem**: Manifest merger failed

**Solution**:
1. Clean project: `./gradlew clean`
2. Invalidate caches: File → Invalidate Caches / Restart

### WasmJS Build Issues

**Problem**: "Module not found"

**Solution**:
1. Clean build: `./gradlew cleanWasmJsBrowserDevelopment`
2. Delete `kotlin-js-store`: `rm -rf kotlin-js-store`
3. Rebuild: `./gradlew wasmJsBrowserDevelopmentRun`

### Memory Issues

**Problem**: Out of memory during build

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

### Slow Build Times

**Optimizations**:
1. Enable Gradle daemon (default)
2. Enable configuration cache (if compatible)
3. Use parallel builds:
   ```properties
   org.gradle.parallel=true
   ```
4. Use build cache:
   ```properties
   org.gradle.caching=true
   ```

## Development Workflow

### Typical Development Cycle

1. **Start Development Server**:
   ```bash
   ./gradlew wasmJsBrowserDevelopmentRun
   # Or run Android configuration in IDE
   ```

2. **Make Code Changes**:
   - Edit files in `shared/src/commonMain/`
   - Changes apply to all platforms

3. **See Changes**:
   - Web: Auto-reloads
   - Android: Rebuild and redeploy

4. **Test**:
   - Manual testing in browser/emulator
   - Run unit tests if added

5. **Commit**:
   ```bash
   git add .
   git commit -m "Description"
   git push
   ```

### Hot Reload / Live Reload

**Web (WasmJS)**:
- Automatic with development server
- Fast incremental compilation
- Browser auto-refresh

**Android**:
- Use Apply Changes (lightning bolt icon) in Android Studio
- Faster than full rebuild
- Limited to certain changes

## Backend Setup (Optional)

### Running Local Backend

If you have the backend repository:

1. **Start Backend**:
   ```bash
   cd backend-repo
   python -m uvicorn main:app --reload
   ```

2. **Verify**:
   - API available at `http://127.0.0.1:8000`
   - Docs at `http://127.0.0.1:8000/docs`

3. **Update OpenAPI Spec**:
   - Download from `http://127.0.0.1:8000/openapi.json`
   - Replace `Achi_openapi.json`
   - Regenerate client code

### Using Production Backend

Default configuration uses ngrok tunnel to production:
- No local backend needed
- Public access via `https://lucky-eminent-gator.ngrok-free.app`
- May require authentication for write operations

## Next Steps

After setup:
1. Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand code structure
2. Review [FEATURES.md](FEATURES.md) to see what's implemented
3. Check [UI_STRUCTURE.md](UI_STRUCTURE.md) for UI patterns
4. Explore [API_INTEGRATION.md](API_INTEGRATION.md) for backend details
5. Study [DATA_MODELS.md](DATA_MODELS.md) for data structures

## Getting Help

**Resources**:
- Kotlin Multiplatform docs: https://kotlinlang.org/docs/multiplatform.html
- Compose Multiplatform docs: https://www.jetbrains.com/lp/compose-multiplatform/
- Ktor docs: https://ktor.io/
- Material 3 guidelines: https://m3.material.io/

**Common Issues**:
- Check existing issues in repository
- Review troubleshooting section above
- Check Gradle build output for errors
- Verify all prerequisites are installed

## Performance Tips

- Use Gradle daemon (enabled by default)
- Close unused Android emulators
- Increase IDE memory: Help → Edit Custom VM Options
- Use SSD for project location
- Keep only active branches checked out
- Regularly clean build directories

