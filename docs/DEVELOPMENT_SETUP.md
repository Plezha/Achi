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
- **Target SDK**: 36
- **Compile SDK**: 36
- **Installation**: Via Android Studio or command line tools

#### IDE

**Recommended**: IntelliJ IDEA or Android Studio
- **IntelliJ IDEA**: 2024.1 or later
- **Android Studio**: Latest stable

**Required Plugins**:
- Kotlin Multiplatform Mobile (KMM)
- Kotlin
- Compose Multiplatform
- Android support (for Android Studio)

### Optional Software

**For Web Development**:
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Browser DevTools for debugging

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

**Build Project**:
```bash
./gradlew build
```

**First build will**:
- Download Gradle dependencies
- Generate OpenAPI client code
- Compile Kotlin code for all targets

**Expected Duration**: 2-5 minutes (first time)

### 4. Verify Setup

```bash
# Check all tasks
./gradlew tasks

# Clean build
./gradlew clean build
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
   - Install Android SDK 36
   - Install Android Build Tools

3. **Local Properties** (if needed):
   
   Create `local.properties` in project root:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

#### Run on Android

**Via Android Studio**:
1. Select "shared" module
2. Choose device/emulator
3. Click Run button

**Via Command Line**:
```bash
# Install on connected device
./gradlew installDebug

# Build APK
./gradlew assembleDebug
# Output: shared/build/outputs/apk/debug/shared-debug.apk
```

### Web (WasmJS)

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

**Production Build**:
```bash
./gradlew wasmJsBrowserProductionWebpack
```

**Output**: `shared/build/dist/wasmJs/productionExecutable/`

## IDE Configuration

### Kotlin Plugin

1. Settings → Plugins
2. Search "Kotlin"
3. Install/Update to latest version
4. Restart IDE

### Compose Multiplatform Plugin

1. Settings → Plugins
2. Search "Compose Multiplatform"
3. Install
4. Restart IDE

## Environment Variables

### API Base URL

Default base URL from OpenAPI spec. To change for local development:

1. Modify `ApiConfig.kt`:
```kotlin
object ApiConfig {
    val baseUrl: String = "http://10.0.2.2:8000"  // Android emulator localhost
}
```

2. Or update `Achi_openapi.json` and regenerate

### No Required Environment Variables

The app doesn't require environment variables for basic development.

## Common Build Tasks

### Cleaning

```bash
# Clean all builds
./gradlew clean

# Clean specific
rm -rf shared/build/generated/openapi
./gradlew openApiGenerate
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

# After updating Achi_openapi.json
./gradlew clean openApiGenerate build
```

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

### WasmJS Build Issues

**Problem**: "Module not found"

**Solution**:
1. Clean build: `./gradlew clean`
2. Delete `kotlin-js-store`: `rm -rf kotlin-js-store`
3. Rebuild: `./gradlew wasmJsBrowserDevelopmentRun`

### Memory Issues

**Problem**: Out of memory during build

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
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

4. **Test with Backend**:
   - Use debug login (user1/password1) on Profile tab
   - Or register new account

### Hot Reload / Live Reload

**Web (WasmJS)**:
- Automatic with development server
- Fast incremental compilation

**Android**:
- Use Apply Changes in Android Studio
- Or full rebuild

## Backend Setup (Optional)

### Using Production Backend

Default configuration uses server from OpenAPI spec:
- No local backend needed
- Requires network access

### Running Local Backend

If you have the backend repository:

1. **Start Backend**:
   ```bash
   cd backend-repo
   python -m uvicorn main:app --reload --host 0.0.0.0
   ```

2. **Update API Config**:
   - Android emulator: `http://10.0.2.2:8000`
   - Web/desktop: `http://localhost:8000`

## Next Steps

After setup:
1. Read [ARCHITECTURE.md](ARCHITECTURE.md) for code structure
2. Review [FEATURES.md](FEATURES.md) for what's implemented
3. Check [UI_STRUCTURE.md](UI_STRUCTURE.md) for UI patterns
4. Explore [API_INTEGRATION.md](API_INTEGRATION.md) for backend details
5. Study [DATA_MODELS.md](DATA_MODELS.md) for data structures

## Resources

- Kotlin Multiplatform docs: https://kotlinlang.org/docs/multiplatform.html
- Compose Multiplatform docs: https://www.jetbrains.com/lp/compose-multiplatform/
- Navigation 3: https://developer.android.com/guide/navigation/design/type-safety
- Ktor docs: https://ktor.io/
- Material 3 guidelines: https://m3.material.io/
