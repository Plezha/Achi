package com.plezha.achi.shared

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_cup_filled
import achi.shared.generated.resources.ic_list
import achi.shared.generated.resources.ic_plus
import achi.shared.generated.resources.ic_plus_filled_outside
import achi.shared.generated.resources.ic_profile
import achi.shared.generated.resources.ic_profile_filled
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementPackRepositoryImpl
import com.plezha.achi.shared.data.AchievementRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Serializable private object AchievementPackListTopRoute
@Serializable private object AddTopRoute
@Serializable private object ProfileTopRoute
@Serializable private object AchievementPackListRoute
@Serializable private object AddRoute
@Serializable private object CreateAchievementPackRoute
@Serializable private object CreateAchievementRoute
@Serializable private object ProfileRoute
@Serializable private data class AchievementRoute(val id: String)
@Serializable private data class AchievementListRoute(val id: String)

data class TopLevelRoute<T>(val route: T, val iconUnselected: DrawableResource, val iconSelected: DrawableResource, val label: String)

private val topLevelRoutes = listOf(
    TopLevelRoute(AddTopRoute, Res.drawable.ic_plus, Res.drawable.ic_plus_filled_outside, "Add"),
    TopLevelRoute(AchievementPackListTopRoute, Res.drawable.ic_list, Res.drawable.ic_cup_filled, "Achievements"),
    TopLevelRoute(ProfileTopRoute, Res.drawable.ic_profile, Res.drawable.ic_profile_filled, "Profile"),
)

@Composable
fun AchiApp() {
    AchiTheme {
        AchiAppNav()
    }
}

@Composable
private fun AchiAppNav() {
//    val usersApi = remember { UsersApi(httpClientEngine = httpClientEngine) }
    val achievementsApi = remember { AchievementsApi(httpClientEngine = httpClientEngine) }
    val packsApi = remember { PacksApi(httpClientEngine = httpClientEngine) }
    val uploadApi = remember { UploadApi(httpClientEngine = httpClientEngine) }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val achievementRepository = remember { MockAchievementRepository(
        achievementsApi = achievementsApi
    ) }
    val achievementPackRepository = remember { AchievementPackRepositoryImpl(
        achievementsApi = achievementsApi,
        packsApi = packsApi,
        uploadApi = uploadApi
    ) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            startDestination = topLevelRoutes[1].route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            addAchievementsNav(
                navController = navController,
                showMessage = {
                    snackbarHostState.showSnackbar(it)
                },
                achievementPackRepository = achievementPackRepository
            )
            achievementListNav(
                navController = navController,
                achievementPackRepository = achievementPackRepository,
                achievementRepository = achievementRepository
            )
            profileNav(
                packsRepository = achievementPackRepository,
                achievementRepository = achievementRepository
            )
        }
    }
}

private fun NavGraphBuilder.addAchievementsNav(
    navController: NavHostController,
    showMessage: suspend CoroutineScope.(String) -> Unit,
    achievementPackRepository: AchievementPackRepository,
) {
    navigation<AddTopRoute>(
        startDestination = AddRoute,
    ) {
        val addAchievementsViewModel = AddAchievementsViewModel(
            achievementPackRepository
        )
        composable<AddRoute> {
            LaunchedEffect(Unit) {
                addAchievementsViewModel.navigationFlow.collectLatest { event ->
                    when (event) {
                        is NavigationEvent.NavigateToCreatePack -> {
                            navController.navigate(CreateAchievementPackRoute)
                        }
                        is NavigationEvent.NavigateToCreateAchievement -> {
                            navController.navigate(CreateAchievementRoute)
                        }
                    }
                }
            }

            AddAchievementScreen(
                addAchievementViewModel = addAchievementsViewModel,
                showMessage = showMessage
            )
        }

        composable<CreateAchievementPackRoute> {
            val createAchievementPackViewModel = CreateAchievementPackViewModel()
            CreateAchievementPackScreen(createAchievementPackViewModel)
        }

        composable<CreateAchievementRoute> {

        }
    }
}

private fun NavGraphBuilder.profileNav(
    packsRepository: AchievementPackRepositoryImpl,
    achievementRepository: AchievementRepository
) {
    navigation<ProfileTopRoute>(
        startDestination = ProfileRoute,
    ) {
        composable<ProfileRoute> {
            val scope = rememberCoroutineScope()
            Box(
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.Default) {
//                            val usersApi = UsersApi(
//                                httpClientEngine = httpClientEngine
//                            )
//                            val pack = AchievementPack(
//                                id = "1",
//                                name = "Pack 1",
//                                count = 5,
//                                achievementIds = achievements.map { it.id },
//                                code = "ABC"
//                            )
//                            val res2 = usersApi.loginTokenPost("user1", "password1")
//                            val token = res2.body().accessToken
//
//                            packsRepository.packsApi.setAccessToken(token)
//                            packsRepository.achievementsApi.setAccessToken(token)
//
//                            val id = packsRepository.createAchievementPack(
//                                name = pack.name,
//                                achievements = achievements.map { achievement ->
//                                    AchievementCreate(
//                                        title = achievement.title,
//                                        shortDescription = achievement.shortDescription,
//                                        steps = achievement.steps.map { step ->
//                                            AchievementStepCreate(
//                                                description = step.description,
//                                                substepsAmount = step.progress.substepsAmount
//                                            )
//                                        },
//                                        longDescription = achievement.longDescription,
//                                        previewImageUrl = achievement.previewImageUrl,
//                                        imageUrl = achievement.imageUrl
//                                    )
//                                },
//                                previewImageUrl = pack.previewImageUrl
//                            )
//                            println(id)
//
//                        val req2 = UserCreate("user1", "password1")
//                        val res2 = api2.loginTokenPost(req2.username, req2.password)
//                        val token = res2.body().accessToken
//
//                        val api = AchievementsApi(
//                            baseUrl = ApiClient.BASE_URL,
//                            httpClientEngine
//                        ).apply {
//                            setAccessToken(token)
//                        }
//                        val req = AchievementCreate(
//                            title = "Test Achievement",
//                            shortDescription = "Test Achievement Short Description",
//                            steps = listOf(AchievementStepCreate("Test Achievement Step 1 Description")),
//                        )
//                        val res: HttpResponse<AchievementSchema> =
//                            api.createAchievementAchievementsPost(req)
//                        println(res.response)
//                        if (res.success) {
//                            println(
//                                api.getAchievementAchievementsAchievementIdGet(res.body().id).response
//                            )
//                        }
                        }
                    }
                ) {
                    Text("Button!")
                }
            }
        }
    }
}

private fun NavGraphBuilder.achievementListNav(
    navController: NavHostController,
    achievementPackRepository: AchievementPackRepository,
    achievementRepository: AchievementRepository
) {
    navigation<AchievementPackListTopRoute>(
        startDestination = AchievementPackListRoute,
    ) {
        composable<AchievementPackListRoute> {
            val achievementPackListViewModel =
                AchievementPackListViewModel(achievementPackRepository)

            AchievementPackList(
                achievementPackListViewModel = achievementPackListViewModel,
                onPackClick = { pack ->
                    navController.navigate(AchievementListRoute(pack.id))
                }
            )
        }
        composable<AchievementListRoute> { backStackEntry ->
            val route: AchievementListRoute = backStackEntry.toRoute()
            val achievementListViewModel = AchievementListViewModel(
                achievementRepository = achievementRepository,
                achievementPackRepository = achievementPackRepository
            ).apply {
                loadAchievementsByPackId(route.id)
            }

            AchievementsScreen(
                achievementListViewModel = achievementListViewModel,
                onAchievementClick = { achievement ->
                    navController.navigate(AchievementRoute(achievement.id))
                },
                onBackClicked = navController::navigateUp
            )
        }
        composable<AchievementRoute> { backStackEntry ->
            val route: AchievementRoute = backStackEntry.toRoute()
            val viewModel = AchievementDetailsViewModel(achievementRepository).apply {
                loadAchievementById(route.id)
            }

            AchievementDetailsScreen(
                viewModel = viewModel,
                onBackClicked = navController::navigateUp
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val selectedRoute =
                currentDestination?.parent?.route ?: currentDestination?.route


        topLevelRoutes.forEach { item ->
            val isSelected =
                    selectedRoute == item.route::class.qualifiedName
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (isSelected) {
                        Icon(
                            imageVector = vectorResource(item.iconSelected),
                            contentDescription = null,
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = vectorResource(item.iconUnselected),
                            contentDescription = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}

expect val httpClientEngine: HttpClientEngine