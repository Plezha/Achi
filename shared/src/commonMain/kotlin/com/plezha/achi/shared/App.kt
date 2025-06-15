package com.plezha.achi.shared

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_plus_filled_outside
import achi.shared.generated.resources.ic_plus
import achi.shared.generated.resources.ic_profile_filled
import achi.shared.generated.resources.ic_profile
import achi.shared.generated.resources.ic_cup_filled
import achi.shared.generated.resources.ic_list
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.plezha.achi.shared.data.MockAchievementPackRepository
import com.plezha.achi.shared.data.MockAchievementRepository
import com.plezha.achi.shared.ui.achievmentdetails.AchievementDetailsScreen
import com.plezha.achi.shared.ui.achievmentdetails.AchievementDetailsViewModel
import com.plezha.achi.shared.ui.achievmentlist.AchievementListViewModel
import com.plezha.achi.shared.ui.achievmentlist.AchievementsScreen
import com.plezha.achi.shared.ui.packlist.AchievementPackList
import com.plezha.achi.shared.ui.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.theme.AchiTheme
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Serializable private object AchievementPackListTopRoute
@Serializable private object AddTopRoute
@Serializable private object ProfileTopRoute
@Serializable private object AchievementPackListRoute
@Serializable private object AddRoute
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
    val navController = rememberNavController()
    val achievementRepository = remember { MockAchievementRepository() }
    val achievementPackRepository = remember { MockAchievementPackRepository() }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            startDestination = topLevelRoutes[1].route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            addAchievementsNav()
            achievementListNav(navController, achievementPackRepository, achievementRepository)
            profileNav()
        }
    }
}

private fun NavGraphBuilder.addAchievementsNav() {
    navigation<AddTopRoute>(
        startDestination = AddRoute,
    ) {
        composable<AddRoute> {
            Text("WIP")
        }
    }
}

private fun NavGraphBuilder.profileNav() {
    navigation<ProfileTopRoute>(
        startDestination = ProfileRoute,
    ) {
        composable<ProfileRoute> {
            Text("WIP")
        }
    }
}

private fun NavGraphBuilder.achievementListNav(
    navController: NavHostController,
    achievementPackRepository: MockAchievementPackRepository,
    achievementRepository: MockAchievementRepository
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
                    navController.navigate(AchievementListRoute(pack))
                }
            )
        }
        composable<AchievementListRoute> { backStackEntry ->
            val pack = backStackEntry.destination
            val achievementListViewModel =
                AchievementListViewModel(achievementRepository)

            AchievementsScreen(
                achievementListViewModel = achievementListViewModel,
                onAchievementClick = { id ->
                    navController.navigate(AchievementRoute(id))
                },
                onBackClicked = {
                    navController.navigateUp()
                }
            )
        }
        composable<AchievementRoute> { backStackEntry ->
            val achievementId: AchievementRoute = backStackEntry.toRoute()
            val viewModel = AchievementDetailsViewModel(achievementRepository).apply {
                loadAchievementById(achievementId.id)
            }

            AchievementDetailsScreen(
                viewModel = viewModel,
                onBackClicked = {
                    navController.navigateUp()
                }
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
            println("$selectedRoute, ${item.route}")
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