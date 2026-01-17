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
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsScreen
import com.plezha.achi.shared.ui.list.achievmentdetails.AchievementDetailsViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementListViewModel
import com.plezha.achi.shared.ui.list.achievmentlist.AchievementsScreen
import com.plezha.achi.shared.ui.list.packlist.AchievementPackList
import com.plezha.achi.shared.ui.list.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.profile.ProfileScreen
import com.plezha.achi.shared.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

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
            AchievementPackListViewModel(appModule.achievementPackRepository)
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
                achievementPackRepository = appModule.achievementPackRepository
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
            AchievementDetailsViewModel(appModule.achievementRepository).apply {
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
        val profileViewModel = remember {
            ProfileViewModel(appModule.authRepository)
        }
        
        LaunchedEffect(Unit) {
            profileViewModel.messageFlow.collectLatest { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        
        ProfileScreen(viewModel = profileViewModel)
    }
}
