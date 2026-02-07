package com.plezha.achi.shared.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.plezha.achi.shared.di.AppModule
import com.plezha.achi.shared.ui.add.AddAchievementScreen
import com.plezha.achi.shared.ui.add.AddAchievementsViewModel
import com.plezha.achi.shared.ui.add.AchievementId
import com.plezha.achi.shared.ui.add.CreateAchievementPackScreen
import com.plezha.achi.shared.ui.add.CreateAchievementPackViewModel
import com.plezha.achi.shared.ui.add.CreatePackNavigationEvent
import com.plezha.achi.shared.ui.add.EditAchievementNavigationEvent
import com.plezha.achi.shared.ui.add.EditAchievementScreen
import com.plezha.achi.shared.ui.add.EditAchievementViewModel
import com.plezha.achi.shared.ui.add.NavigationEvent
import com.plezha.achi.shared.ui.list.achievementdetails.AchievementDetailsScreen
import com.plezha.achi.shared.ui.list.achievementdetails.AchievementDetailsViewModel
import com.plezha.achi.shared.ui.list.achievementlist.AchievementListViewModel
import com.plezha.achi.shared.ui.list.achievementlist.AchievementsScreen
import com.plezha.achi.shared.ui.list.packlist.AchievementPackList
import com.plezha.achi.shared.ui.list.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.debug.DebugPanelScreen
import com.plezha.achi.shared.ui.profile.ProfileScreen
import com.plezha.achi.shared.ui.profile.ProfileViewModel
import com.plezha.achi.shared.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.collectLatest

/**
 * Pops the back stack if there is more than one entry.
 */
private fun NavBackStack<NavKey>.popBack() {
    if (size > 1) {
        removeAt(size - 1)
    }
}

/**
 * Creates the navigation entry provider with all screen entries.
 */
@Composable
fun createNavEntryProvider(
    appModule: AppModule,
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState,
    addAchievementsViewModel: AddAchievementsViewModel,
    createAchievementPackViewModel: CreateAchievementPackViewModel
) = entryProvider {
    // Add screen
    entry<AddRoute> {
        LaunchedEffect(Unit) {
            addAchievementsViewModel.navigationFlow.collectLatest { event ->
                when (event) {
                    is NavigationEvent.NavigateToCreatePack -> {
                        backStack.add(CreateAchievementPackRoute)
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
                        backStack.popBack()
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
            onBackClicked = backStack::popBack,
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
                achievementId = achievementData?.id ?: AchievementId(""),
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
                        backStack.popBack()
                    }
                    is EditAchievementNavigationEvent.Cancel -> {
                        backStack.popBack()
                    }
                }
            }
        }
        
        EditAchievementScreen(
            viewModel = editViewModel,
            onBackClicked = backStack::popBack
        )
    }

    // Achievement Pack List screen
    entry<AchievementPackListRoute> {
        val achievementPackListViewModel = remember {
            AchievementPackListViewModel(
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository,
                achievementRepository = appModule.achievementRepository
            )
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
                achievementRepository = appModule.achievementRepository,
                achievementPackRepository = appModule.achievementPackRepository,
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository
            ).apply {
                loadAchievementsByPackId(route.id)
            }
        }

        AchievementsScreen(
            achievementListViewModel = achievementListViewModel,
            onAchievementClick = { achievement ->
                backStack.add(AchievementRoute(achievement.id))
            },
            onBackClicked = backStack::popBack,
            onRetry = { achievementListViewModel.loadAchievementsByPackId(route.id) }
        )
    }

    // Achievement Details screen
    entry<AchievementRoute> { route ->
        val viewModel = remember(route.id) {
            AchievementDetailsViewModel(
                repository = appModule.achievementRepository,
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository
            ).apply {
                loadAchievementById(route.id)
            }
        }

        AchievementDetailsScreen(
            viewModel = viewModel,
            onBackClicked = backStack::popBack,
            onRetry = { viewModel.loadAchievementById(route.id) }
        )
    }

    // Profile screen
    entry<ProfileRoute> {
        val profileViewModel = remember {
            ProfileViewModel(appModule.authRepository)
        }
        
        LaunchedEffect(Unit) {
            profileViewModel.messageFlow.collectLatest { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        
        ProfileScreen(
            viewModel = profileViewModel,
            onNavigateToSettings = { backStack.add(SettingsRoute) }
        )
    }

    // Settings screen
    entry<SettingsRoute> {
        SettingsScreen(
            onBackClicked = backStack::popBack,
            onNavigateToDebugPanel = { backStack.add(DebugPanelRoute) }
        )
    }

    // Debug panel screen (debug builds only, gated in SettingsScreen)
    entry<DebugPanelRoute> {
        val profileViewModel = remember {
            ProfileViewModel(appModule.authRepository)
        }

        LaunchedEffect(Unit) {
            profileViewModel.messageFlow.collectLatest { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        DebugPanelScreen(
            onBackClicked = backStack::popBack,
            onDebugLogin = profileViewModel::onDebugLogin
        )
    }
}
