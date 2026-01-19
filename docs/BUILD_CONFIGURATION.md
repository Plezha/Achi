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

## Version Catalog

**File**: `gradle/libs.versions.toml`

### Versions

```toml
[versions]
agp = "8.9.1"
kotlin = "2.3.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
kotlinxSerializationJson = "1.9.0"
kotlinxDatetime = "0.7.0"
ktor = "3.1.0"
lifecycleRuntimeKtx = "2.9.2"
activityCompose = "1.10.0"
composeMultiplatform = "1.10.0"
kotlinTest = "2.3.0"
navigation3-ui = "1.0.0-alpha05"
multiplatformSettings = "1.3.0"
```

### Libraries

```toml
[libraries]
# Navigation 3 multiplatform
jetbrains-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3-ui" }

# Ktor Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# Serialization & DateTime
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }

# Multiplatform Settings
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatformSettings" }
multiplatform-settings-no-arg = { module = "com.russhwolf:multiplatform-settings-no-arg", version.ref = "multiplatformSettings" }

# Android
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
kotlin-test = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlinTest" }
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

## Shared Module Configuration

**File**: `shared/build.gradle.kts`

### Plugins

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.android.application)
    id("org.openapi.generator") version "7.18.0"
    kotlin("plugin.serialization") version "2.3.0"
}
```

### Kotlin Multiplatform Configuration

#### Android Target

```kotlin
kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}
```

#### WasmJS Target

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
                    add(rootDirPath)
                    add(projectDirPath)
                }
            }
        }
    }
    binaries.executable()
}
```

#### iOS Targets (Prepared, Not Active)

```kotlin
val xcfName = "sharedKit"

listOf<KotlinNativeTarget>(
//  iosX64(),
//  iosArm64(),
//  iosSimulatorArm64()
).forEach { iosTarget ->
    iosTarget.binaries.framework {
        baseName = xcfName
        isStatic = true
    }
}
```

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
            
            // Navigation 3
            implementation(libs.jetbrains.navigation3.ui)
            
            // Serialization & DateTime
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Settings (for auth persistence)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            
            // Collections
            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
            
            // FileKit
            val filekitversion = "0.12.0"
            implementation("io.github.vinceglb:filekit-core:${filekitversion}")
            implementation("io.github.vinceglb:filekit-dialogs:${filekitversion}")
            implementation("io.github.vinceglb:filekit-dialogs-compose:${filekitversion}")
            implementation("io.github.vinceglb:filekit-coil:${filekitversion}")
            
            // Coil
            implementation("io.coil-kt.coil3:coil-compose:3.1.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.1.0")
        }
        kotlin.srcDir("${buildDir}/generated/openapi/src/commonMain/kotlin")
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
}
```

### Android Configuration

```kotlin
android {
    namespace = "com.plezha.achi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.plezha.achi"
        minSdk = 24
        targetSdk = 36
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}
```

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

## Build Variants

### Android Build Types

**Debug**:
- Debuggable: Yes
- Minification: No
- Signing: Debug keystore

**Release**:
- Debuggable: No
- Minification: Currently disabled
- Signing: Debug keystore (for development)

### WasmJS Build Modes

**Development**:
- Source maps: Yes
- Minification: No
- Dev server: Included

**Production**:
- Source maps: Optional
- Minification: Yes
- Optimized bundle size

## Output Locations

**Android APK**:
- Debug: `shared/build/outputs/apk/debug/shared-debug.apk`
- Release: `shared/build/outputs/apk/release/shared-release.apk`

**WasmJS**:
- Development: Served by Webpack dev server
- Production: `shared/build/dist/wasmJs/productionExecutable/`

**Generated API Code**:
- `shared/build/generated/openapi/src/commonMain/kotlin/`

## Version Summary

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.10.0 |
| Android Gradle Plugin | 8.9.1 |
| Navigation 3 | 1.0.0-alpha05 |
| Ktor | 3.1.0 |
| Coil | 3.1.0 |
| FileKit | 0.12.0 |
| OpenAPI Generator | 7.18.0 |
| Multiplatform Settings | 1.3.0 |
| Android Compile SDK | 36 |
| Android Min SDK | 24 |
| Android Target SDK | 36 |
