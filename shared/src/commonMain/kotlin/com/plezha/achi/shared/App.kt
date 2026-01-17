package com.plezha.achi.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.plezha.achi.shared.di.AppModule
import com.plezha.achi.shared.navigation.AchievementPackListRoute
import com.plezha.achi.shared.navigation.BottomNavigationBar
import com.plezha.achi.shared.navigation.createNavEntryProvider
import com.plezha.achi.shared.navigation.navSavedStateConfig
import com.plezha.achi.shared.ui.add.AddAchievementsViewModel
import com.plezha.achi.shared.ui.add.CreateAchievementPackViewModel
import com.plezha.achi.shared.ui.theme.AchiTheme
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.ktor.client.engine.HttpClientEngine

@Composable
fun AchiApp() {
    // Configure Coil to support PlatformFile from FileKit
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .build()
    }

    AchiTheme {
        AchiAppNav()
    }
}

@Composable
private fun AchiAppNav() {
    // Create DI container
    val appModule = remember { AppModule(httpClientEngine) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation 3: User-owned back stack
    val backStack = rememberNavBackStack(navSavedStateConfig, AchievementPackListRoute)

    // Shared ViewModels for pack creation flow
    val addAchievementsViewModel = remember { 
        AddAchievementsViewModel(appModule.achievementPackRepository) 
    }
    val createAchievementPackViewModel = remember { 
        CreateAchievementPackViewModel(appModule.achievementPackRepository) 
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = backStack.lastOrNull(),
                onNavigate = { route ->
                    // Clear stack and navigate to top-level destination
                    while (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                    if (backStack.lastOrNull() != route) {
                        backStack[0] = route
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            entryProvider = createNavEntryProvider(
                appModule = appModule,
                backStack = backStack,
                snackbarHostState = snackbarHostState,
                addAchievementsViewModel = addAchievementsViewModel,
                createAchievementPackViewModel = createAchievementPackViewModel
            )
        )
    }
}

expect val httpClientEngine: HttpClientEngine
