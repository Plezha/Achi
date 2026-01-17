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
import com.plezha.achi.shared.data.network.apis.AuthenticationApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.data.network.apis.UsersApi
import com.plezha.achi.shared.data.network.models.UserCreateBody
import com.plezha.achi.shared.ui.add.AddAchievementScreen
import com.plezha.achi.shared.ui.add.AddAchievementsViewModel
import com.plezha.achi.shared.ui.add.CreateAchievementPackScreen
import com.plezha.achi.shared.ui.add.CreateAchievementPackViewModel
import com.plezha.achi.shared.ui.add.CreatePackNavigationEvent
import com.plezha.achi.shared.ui.add.EditAchievementNavigationEvent
import com.plezha.achi.shared.ui.add.EditAchievementScreen
import com.plezha.achi.shared.ui.add.EditAchievementViewModel
import com.plezha.achi.shared.ui.add.NavigationEvent
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsScreen
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementListViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementsScreen
import com.plezha.achi.shared.ui.list.packlist.AchievementPackList
import com.plezha.achi.shared.ui.list.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.theme.AchiTheme
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
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

// API Base URL for testing (Android emulator uses 10.0.2.2 to access host's localhost)
private const val API_BASE_URL = "http://10.0.2.2:8000"

// Hard-coded test credentials
private const val TEST_USERNAME = "user1"
private const val TEST_PASSWORD = "password1"

// Navigation 3 routes - all implement NavKey
@Serializable
data object AchievementPackListRoute : NavKey

@Serializable
data object AddRoute : NavKey

@Serializable
data object CreateAchievementPackRoute : NavKey

@Serializable
data class EditAchievementRoute(val achievementIndex: Int) : NavKey

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
            subclass(EditAchievementRoute::class, EditAchievementRoute.serializer())
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
    val usersApi = remember { UsersApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine) }
    val authApi = remember { AuthenticationApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine) }
    val achievementsApi = remember { AchievementsApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine) }
    val packsApi = remember { PacksApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine) }
    val uploadApi = remember { UploadApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Register and authenticate on app start with hard-coded credentials
    LaunchedEffect(Unit) {
        try {
            // First, try to register the user
            val registerResponse = usersApi.registerUserUsersPost(
                UserCreateBody(
                    username = TEST_USERNAME,
                    password = TEST_PASSWORD,
                    displayName = "Test User"
                )
            )
            val registerStatus = registerResponse.response.status
            println("Register response: $registerStatus")
            if (registerResponse.success) {
                snackbarHostState.showSnackbar("Registered: $TEST_USERNAME")
            } else {
                // 409 = user already exists, which is fine
                println("Register failed (may already exist): $registerStatus")
            }
            
            // Then login
            val loginResponse = authApi.loginTokenPost(TEST_USERNAME, TEST_PASSWORD)
            val loginStatus = loginResponse.response.status
            println("Login response: $loginStatus")
            
            if (loginResponse.success) {
                val token = loginResponse.body().accessToken
                println("Got token: ${token.take(20)}...")
                // Set token on all APIs that require authentication
                achievementsApi.setAccessToken(token)
                packsApi.setAccessToken(token)
                uploadApi.setAccessToken(token)
                snackbarHostState.showSnackbar("Logged in as $TEST_USERNAME")
            } else {
                snackbarHostState.showSnackbar("Login failed: $loginStatus")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            snackbarHostState.showSnackbar("Auth error: ${e.message}")
        }
    }
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
    
    // Shared ViewModel for pack creation flow (shared between CreateAchievementPackScreen and EditAchievementScreen)
    val createAchievementPackViewModel = remember { 
        CreateAchievementPackViewModel(achievementPackRepository) 
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
                                    // No longer used - achievement creation is now part of pack creation flow
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
                    LaunchedEffect(Unit) {
                        createAchievementPackViewModel.navigationFlow.collectLatest { event ->
                            when (event) {
                                is CreatePackNavigationEvent.NavigateBack -> {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                }
                            }
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        createAchievementPackViewModel.messageFlow.collectLatest { message ->
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                    
                    CreateAchievementPackScreen(
                        createAchievementPackViewModel = createAchievementPackViewModel,
                        onBackClicked = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        },
                        onAchievementClick = { index ->
                            backStack.add(EditAchievementRoute(index))
                        }
                    )
                }

                // Edit Achievement screen (part of pack creation flow)
                entry<EditAchievementRoute> { route ->
                    val achievementData = createAchievementPackViewModel.getAchievementAtIndex(route.achievementIndex)
                    val editViewModel = remember(route.achievementIndex) {
                        EditAchievementViewModel(
                            achievementId = achievementData?.id ?: com.plezha.achi.shared.ui.add.AchievementId(""),
                            initialData = achievementData
                        )
                    }
                    
                    LaunchedEffect(Unit) {
                        editViewModel.navigationFlow.collectLatest { event ->
                            when (event) {
                                is EditAchievementNavigationEvent.SaveAndNavigateBack -> {
                                    createAchievementPackViewModel.updateAchievementAtIndex(
                                        route.achievementIndex,
                                        event.achievement
                                    )
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                }
                                is EditAchievementNavigationEvent.Cancel -> {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.size - 1)
                                    }
                                }
                            }
                        }
                    }
                    
                    EditAchievementScreen(
                        viewModel = editViewModel,
                        onBackClicked = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        }
                    )
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
                is AddRoute, is CreateAchievementPackRoute, is EditAchievementRoute -> 
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
