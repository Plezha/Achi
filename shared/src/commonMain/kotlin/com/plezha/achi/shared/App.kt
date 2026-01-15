package com.plezha.achi.shared

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_cup_filled
import achi.shared.generated.resources.ic_list
import achi.shared.generated.resources.ic_plus
import achi.shared.generated.resources.ic_plus_filled_outside
import achi.shared.generated.resources.ic_profile
import achi.shared.generated.resources.ic_profile_filled
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.plezha.achi.shared.data.AchievementPackRepositoryImpl
import com.plezha.achi.shared.data.MockAchievementRepository
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.ui.add.AddAchievementScreen
import com.plezha.achi.shared.ui.add.AddAchievementsViewModel
import com.plezha.achi.shared.ui.add.CreateAchievementPackScreen
import com.plezha.achi.shared.ui.add.CreateAchievementPackViewModel
import com.plezha.achi.shared.ui.add.NavigationEvent
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsScreen
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementListViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementsScreen
import com.plezha.achi.shared.ui.list.packlist.AchievementPackList
import com.plezha.achi.shared.ui.list.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.theme.AchiTheme
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

// Navigation 3 routes - all implement NavKey
@Serializable
data object AchievementPackListRoute : NavKey

@Serializable
data object AddRoute : NavKey

@Serializable
data object CreateAchievementPackRoute : NavKey

@Serializable
data object CreateAchievementRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

@Serializable
data class AchievementRoute(val id: String) : NavKey

@Serializable
data class AchievementListRoute(val id: String) : NavKey

// Polymorphic serialization configuration for multiplatform support
private val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AchievementPackListRoute::class, AchievementPackListRoute.serializer())
            subclass(AddRoute::class, AddRoute.serializer())
            subclass(CreateAchievementPackRoute::class, CreateAchievementPackRoute.serializer())
            subclass(CreateAchievementRoute::class, CreateAchievementRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(AchievementRoute::class, AchievementRoute.serializer())
            subclass(AchievementListRoute::class, AchievementListRoute.serializer())
        }
    }
}

data class TopLevelRoute<T : NavKey>(
    val route: T,
    val iconUnselected: DrawableResource,
    val iconSelected: DrawableResource,
    val label: String
)

private val topLevelRoutes = listOf(
    TopLevelRoute(AddRoute, Res.drawable.ic_plus, Res.drawable.ic_plus_filled_outside, "Add"),
    TopLevelRoute(AchievementPackListRoute, Res.drawable.ic_list, Res.drawable.ic_cup_filled, "Achievements"),
    TopLevelRoute(ProfileRoute, Res.drawable.ic_profile, Res.drawable.ic_profile_filled, "Profile"),
)

@Composable
fun AchiApp() {
    AchiTheme {
        AchiAppNav()
    }
}

@Composable
private fun AchiAppNav() {
    val achievementsApi = remember { AchievementsApi(httpClientEngine = httpClientEngine) }
    val packsApi = remember { PacksApi(httpClientEngine = httpClientEngine) }
    val uploadApi = remember { UploadApi(httpClientEngine = httpClientEngine) }

    val snackbarHostState = remember { SnackbarHostState() }
    val achievementRepository = remember {
        MockAchievementRepository(achievementsApi = achievementsApi)
    }
    val achievementPackRepository = remember {
        AchievementPackRepositoryImpl(
            achievementsApi = achievementsApi,
            packsApi = packsApi,
            uploadApi = uploadApi
        )
    }

    // Navigation 3: User-owned back stack
    val backStack = rememberNavBackStack(navSavedStateConfig, AchievementPackListRoute)

    // ViewModels - in Navigation 3 we manage these ourselves
    val addAchievementsViewModel = remember { AddAchievementsViewModel(achievementPackRepository) }

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
            entryProvider = entryProvider {
                // Add screen
                entry<AddRoute> {
                    LaunchedEffect(Unit) {
                        addAchievementsViewModel.navigationFlow.collectLatest { event ->
                            when (event) {
                                is NavigationEvent.NavigateToCreatePack -> {
                                    backStack.add(CreateAchievementPackRoute)
                                }
                                is NavigationEvent.NavigateToCreateAchievement -> {
                                    backStack.add(CreateAchievementRoute)
                                }
                            }
                        }
                    }

                    AddAchievementScreen(
                        addAchievementViewModel = addAchievementsViewModel,
                        showMessage = { message ->
                            snackbarHostState.showSnackbar(message)
                        }
                    )
                }

                // Create Achievement Pack screen
                entry<CreateAchievementPackRoute> {
                    val createAchievementPackViewModel = remember { CreateAchievementPackViewModel() }
                    CreateAchievementPackScreen(createAchievementPackViewModel)
                }

                // Create Achievement screen (empty for now)
                entry<CreateAchievementRoute> {
                    // TODO: Implement CreateAchievementScreen
                }

                // Achievement Pack List screen
                entry<AchievementPackListRoute> {
                    val achievementPackListViewModel = remember {
                        AchievementPackListViewModel(achievementPackRepository)
                    }

                    AchievementPackList(
                        achievementPackListViewModel = achievementPackListViewModel,
                        onPackClick = { pack ->
                            backStack.add(AchievementListRoute(pack.id))
                        }
                    )
                }

                // Achievement List screen
                entry<AchievementListRoute> { route ->
                    val achievementListViewModel = remember(route.id) {
                        AchievementListViewModel(
                            achievementRepository = achievementRepository,
                            achievementPackRepository = achievementPackRepository
                        ).apply {
                            loadAchievementsByPackId(route.id)
                        }
                    }

                    AchievementsScreen(
                        achievementListViewModel = achievementListViewModel,
                        onAchievementClick = { achievement ->
                            backStack.add(AchievementRoute(achievement.id))
                        },
                        onBackClicked = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                    )
                }

                // Achievement Details screen
                entry<AchievementRoute> { route ->
                    val viewModel = remember(route.id) {
                        AchievementDetailsViewModel(achievementRepository).apply {
                            loadAchievementById(route.id)
                        }
                    }

                    AchievementDetailsScreen(
                        viewModel = viewModel,
                        onBackClicked = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                    )
                }

                // Profile screen
                entry<ProfileRoute> {
                    val scope = rememberCoroutineScope()
                    Box(contentAlignment = Alignment.Center) {
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.Default) {
                                    // Profile action placeholder
                                }
                            }
                        ) {
                            Text("Button!")
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    NavigationBar {
        topLevelRoutes.forEach { item ->
            val isSelected = when (currentRoute) {
                is AddRoute, is CreateAchievementPackRoute, is CreateAchievementRoute -> 
                    item.route is AddRoute
                is AchievementPackListRoute, is AchievementListRoute, is AchievementRoute -> 
                    item.route is AchievementPackListRoute
                is ProfileRoute -> 
                    item.route is ProfileRoute
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = vectorResource(
                            if (isSelected) item.iconSelected else item.iconUnselected
                        ),
                        contentDescription = null,
                        modifier = Modifier.height(24.dp)
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}

expect val httpClientEngine: HttpClientEngine
