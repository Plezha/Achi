# Build Configuration Documentation

## Overview

The Achi project uses Gradle with Kotlin DSL for build configuration. It leverages Kotlin Multiplatform with Compose Multiplatform to target Android and WasmJS platforms. The build system includes OpenAPI code generation, version catalogs, and platform-specific configurations.

## Project Structure

### Root Build Configuration

**File**: `build.gradle.kts` (project root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
```

**Purpose**:
- Declares plugins for all submodules
- `apply false` makes plugins available without applying to root
- Uses version catalog references (`libs.plugins.*`)

### Settings Configuration

**File**: `settings.gradle.kts`

```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Achi"
include(":shared")
```

**Key Features**:
- **TYPESAFE_PROJECT_ACCESSORS**: Type-safe references to subprojects
- **Repository Configuration**: 
  - `google()`: Android and Compose dependencies
  - `mavenCentral()`: Kotlin and third-party libraries
  - `gradlePluginPortal()`: Gradle plugins
- **Module Structure**: Single `shared` module

### Gradle Properties

**File**: `gradle.properties`

Common properties:
```properties
org.gradle.jvmargs=-Xmx4g
kotlin.code.style=official
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.parallel=true
org.gradle.caching=true
```

**Performance Optimizations**:
- `jvmargs`: Increases Gradle JVM memory to 4GB
- `parallel`: Enables parallel execution
- `caching`: Enables build cache

**Android Settings**:
- `useAndroidX`: Uses AndroidX libraries
- `nonTransitiveRClass`: Optimizes R class generation

## Version Catalog

**File**: `gradle/libs.versions.toml`

Centralized dependency management using Gradle Version Catalogs.

### Versions

```toml
[versions]
agp = "8.8.2"                          # Android Gradle Plugin
kotlin = "2.2.0"                       # Kotlin version
junit = "4.13.2"                       # Testing
junitVersion = "1.2.1"                 # AndroidX Test
espressoCore = "3.6.1"                 # UI Testing
kotlinxSerializationJson = "1.8.0"    # JSON serialization
ktor = "3.1.0"                         # HTTP client
lifecycleRuntimeKtx = "2.9.2"         # Lifecycle
activityCompose = "1.10.0"            # Compose Activity
androidx-navigation = "2.9.0-beta03"  # Navigation
composeMultiplatform = "1.8.2"        # Compose Multiplatform
kotlinTest = "2.1.21"                  # Kotlin Test
```

**Version Strategy**:
- Use latest stable versions
- Keep Kotlin and Compose versions synchronized
- Update regularly for bug fixes and features

### Libraries

```toml
[libraries]
# Core Android/Compose
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation" }

# Ktor Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
kotlin-test = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlinTest" }

# Serialization
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```

### Plugins

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

**Usage in build files**:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}
dependencies {
    implementation(libs.ktor.client.core)
}
```

## Shared Module Configuration

**File**: `shared/build.gradle.kts`

Main application module containing all code.

### Plugins

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.android.application)
    id("org.openapi.generator") version "7.14.0"
    kotlin("plugin.serialization") version "2.1.0"
}
```

**Plugin Purposes**:
- **kotlinMultiplatform**: Enables KMP
- **composeMultiplatform**: Compose UI framework
- **composeCompiler**: Kotlin compiler plugin for Compose
- **android.application**: Android app configuration
- **openapi.generator**: Generates API client code
- **plugin.serialization**: Kotlin serialization support

### Kotlin Multiplatform Configuration

#### Targets

```kotlin
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    wasmJs {
        outputModuleName = "achi"
        browser {
            commonWebpackConfig {
                outputFileName = "achi.js"
            }
        }
        binaries.executable()
    }
    
    // iOS targets (commented out)
    // iosX64(), iosArm64(), iosSimulatorArm64()
}
```

**Android Target**:
- JVM target: Java 11
- Compiles to Android-compatible bytecode

**WasmJS Target**:
- Output module: `achi`
- Output file: `achi.js`
- Browser configuration with Webpack
- Executable binary type

**iOS Targets** (prepared but inactive):
- Framework name: `sharedKit`
- Static framework
- Would support x64, arm64, and simulator arm64

### Source Sets

```kotlin
sourceSets {
    commonMain {
        dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Navigation & Serialization
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            
            // Other
            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
            
            // FileKit (file picking)
            val filekitversion = "0.10.0"
            implementation("io.github.vinceglb:filekit-core:${filekitversion}")
            implementation("io.github.vinceglb:filekit-dialogs:${filekitversion}")
            implementation("io.github.vinceglb:filekit-dialogs-compose:${filekitversion}")
            implementation("io.github.vinceglb:filekit-coil:${filekitversion}")
        }
        
        // Add generated OpenAPI code to source set
        kotlin.srcDir("${buildDir}/generated/openapi/src/commonMain/kotlin")
    }
    
    commonTest {
        dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.junit)
        }
    }
    
    androidMain {
        dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.ui.test.manifest)
        }
    }
    
    wasmJsMain {
        dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
    
    iosMain {
        dependencies {
            // iOS-specific dependencies would go here
        }
    }
}
```

**Dependency Strategy**:
- **Common**: Shared across all platforms
- **Platform-specific**: Only what's needed per platform
- **Test**: Testing dependencies in test source sets

### Android Configuration

```kotlin
android {
    namespace = "com.plezha.achi"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.plezha.achi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

**Configuration Details**:
- **namespace**: Package name
- **compileSdk**: SDK version to compile against (35 = Android 15)
- **minSdk**: Minimum Android version (24 = Android 7.0)
- **targetSdk**: Target Android version
- **versionCode**: Numeric version for Play Store
- **versionName**: Human-readable version

**Build Types**:
- **debug**: Default, includes debugging symbols
- **release**: Minification disabled (can be enabled)

**Packaging**:
- Excludes duplicate META-INF license files

### WasmJS Configuration

```kotlin
wasmJs {
    outputModuleName = "achi"
    browser {
        val rootDirPath = project.rootDir.path
        val projectDirPath = project.projectDir.path
        commonWebpackConfig {
            outputFileName = "achi.js"
            devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                static = (static ?: mutableListOf()).apply {
                    // Serve sources to debug inside browser
                    add(rootDirPath)
                    add(projectDirPath)
                }
            }
        }
    }
    binaries.executable()
}
```

**Webpack Dev Server**:
- Serves static files from root and project directories
- Enables source map debugging
- Hot reload support
- Output file: `achi.js`

### OpenAPI Code Generation

```kotlin
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(rootProject.file("Achi_openapi.json").toURI().toString())
    outputDir.set("${buildDir}/generated/openapi")
    packageName.set("com.plezha.achi.shared.data.network")
    
    configOptions.set(mapOf(
        "library" to "multiplatform",
        "useCoroutines" to "true",
        "dateLibrary" to "kotlinx-datetime",
        "sourceFolder" to "src/commonMain/kotlin"
    ))
}
```

**Configuration**:
- **generatorName**: `kotlin` - Generate Kotlin code
- **inputSpec**: Path to OpenAPI spec file
- **outputDir**: Where to generate code
- **packageName**: Package for generated code
- **library**: `multiplatform` - KMP support
- **useCoroutines**: Use suspend functions
- **dateLibrary**: Use kotlinx-datetime
- **sourceFolder**: Source directory within output

**Task Integration**:
```kotlin
tasks.withType<KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}
```

All Kotlin compilation depends on OpenAPI generation, ensuring API code is always up-to-date.

**Compatibility Note**:
> This openapigenerator version is poorly compatible with Ktor 3+, some errors might need to be fixed manually

## Build Tasks

### Common Tasks

```bash
# Clean
./gradlew clean

# Build all
./gradlew build

# Assemble (without tests)
./gradlew assemble
```

### Android Tasks

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew testDebugUnitTest

# Android instrumented tests
./gradlew connectedAndroidTest
```

### WasmJS Tasks

```bash
# Development build with server
./gradlew wasmJsBrowserDevelopmentRun

# Production build
./gradlew wasmJsBrowserProductionWebpack

# Distribution build
./gradlew wasmJsBrowserDistribution
```

### OpenAPI Tasks

```bash
# Generate API code
./gradlew openApiGenerate

# Clean generated code
./gradlew cleanOpenApiGenerate
```

### Verification Tasks

```bash
# Run all checks
./gradlew check

# Lint
./gradlew lint

# Detekt (if configured)
./gradlew detekt
```

## Build Variants

### Android Build Types

**Debug** (default):
- Debuggable: Yes
- Minification: No
- Obfuscation: No
- Signing: Debug keystore
- Build speed: Fast

**Release**:
- Debuggable: No
- Minification: Configured (currently disabled)
- Obfuscation: Possible with R8/ProGuard
- Signing: Requires release keystore
- Build speed: Slower
- Optimized for production

### WasmJS Build Modes

**Development**:
- Source maps: Yes
- Minification: No
- Debugging: Full
- Dev server: Included
- Build speed: Fast

**Production**:
- Source maps: Optional
- Minification: Yes
- Dead code elimination: Yes
- Optimized bundle size
- Build speed: Slower

## Dependency Management

### Version Updates

**Check for updates**:
```bash
./gradlew dependencyUpdates
```

**Strategy**:
- Keep Kotlin and Compose in sync
- Update Ktor carefully (API changes)
- Test thoroughly after updates
- Update AndroidX libraries together

### Dependency Resolution

**Conflict resolution** (if needed):
```kotlin
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    }
}
```

### Excluding Dependencies

```kotlin
implementation("some:library:1.0") {
    exclude(group = "com.unwanted", module = "module")
}
```

## Optimization Strategies

### Build Performance

**Gradle Daemon**:
```properties
org.gradle.daemon=true  # Default, keeps Gradle process running
```

**Parallel Execution**:
```properties
org.gradle.parallel=true
```

**Configuration Cache**:
```properties
org.gradle.configuration-cache=true  # Experimental, may break some plugins
```

**Build Cache**:
```properties
org.gradle.caching=true
```

**Increase Memory**:
```properties
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g
```

### Android Optimization

**R8** (code shrinker):
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

**Non-transitive R classes**:
```properties
android.nonTransitiveRClass=true
```

**Disable unused features**:
```kotlin
android {
    buildFeatures {
        aidl = false
        renderScript = false
        shaders = false
    }
}
```

### WasmJS Optimization

Production build automatically:
- Minifies JavaScript
- Optimizes Wasm binary
- Removes dead code
- Generates source maps (optional)

## Multi-Module Structure (Future)

Currently single `shared` module. Potential future structure:

```
Achi/
├── shared/           # Common code
├── androidApp/       # Android-specific
├── webApp/          # Web-specific
├── iosApp/          # iOS-specific
├── core/            # Core business logic
├── data/            # Data layer
└── ui-common/       # Shared UI components
```

Benefits:
- Better separation of concerns
- Faster incremental builds
- Platform-specific optimizations
- Independent versioning

## Signing Configuration (Android)

### Debug Signing

Automatic with debug keystore:
- Location: `~/.android/debug.keystore`
- No configuration needed

### Release Signing

**Create keystore**:
```bash
keytool -genkey -v -keystore release.keystore -alias achi -keyalg RSA -keysize 2048 -validity 10000
```

**Configure in `build.gradle.kts`**:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "achi"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**Security**: Never commit keystore or passwords to version control.

## Continuous Integration

### GitHub Actions Example

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: shared/build/outputs/apk/debug/shared-debug.apk
```

### Build Variants for CI

- Run lint checks
- Execute tests
- Build all variants
- Upload artifacts
- Deploy (if main branch)

## Troubleshooting Build Issues

### Gradle Sync Failed

```bash
./gradlew --refresh-dependencies
./gradlew clean build --stacktrace
```

### Out of Memory

Increase memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx8g -XX:MaxMetaspaceSize=2g
```

### Dependency Resolution Failed

```bash
./gradlew dependencies --configuration releaseRuntimeClasspath
```

Check for version conflicts and force versions if needed.

### Build Cache Issues

```bash
./gradlew cleanBuildCache
rm -rf ~/.gradle/caches
```

### OpenAPI Generation Issues

```bash
rm -rf shared/build/generated/openapi
./gradlew openApiGenerate --stacktrace
```

## Future Build Enhancements

- **KMM Mobile plugin**: Enhanced iOS support
- **Convention plugins**: Reusable build logic
- **Composite builds**: Multiple repositories
- **Custom source sets**: Feature modules
- **BuildSrc**: Kotlin build scripts
- **Version catalogs**: Shared between projects
- **Gradle Kotlin DSL precompiled scripts**: Type-safe build configuration

