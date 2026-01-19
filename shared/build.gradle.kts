@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.android.application)
    id("org.openapi.generator") version "7.18.0"
    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    listOf<KotlinNativeTarget>(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                implementation(libs.jetbrains.navigation3.ui)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
                val filekitversion = "0.12.0"
                implementation("io.github.vinceglb:filekit-core:${filekitversion}")
                implementation("io.github.vinceglb:filekit-dialogs:${filekitversion}")
                implementation("io.github.vinceglb:filekit-dialogs-compose:${filekitversion}")
                implementation("io.github.vinceglb:filekit-coil:${filekitversion}")
                
                implementation("io.coil-kt.coil3:coil-compose:3.1.0")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.1.0")
                
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.2")
            }
            kotlin.srcDir("${buildDir}/generated/openapi/src/commonMain/kotlin")
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
            }
        }

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

        wasmJsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.0")
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)

                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.ui.tooling)
                implementation(libs.androidx.ui.test.manifest)

                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        androidNativeTest {
            dependencies {
                implementation(libs.androidx.ui.test.manifest)
                implementation(libs.androidx.junit)
                implementation(libs.androidx.espresso.core)
                implementation(libs.androidx.ui.test.junit4)

            }
        }

//        getByName("androidDeviceTest") {
//            dependencies {
//                implementation(libs.androidx.runner)
//                implementation(libs.androidx.core)
//                implementation(libs.androidx.junit)
//            }
//        }

        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.0")
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}

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

// This openapigenerator version is poorly compatible with Ktor 3+, some errors might need to be fixed manually
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}