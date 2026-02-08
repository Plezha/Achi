package com.plezha.achi.shared.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.plezha.achi.shared.ui.list.achievementlist.AchievementListNavigationEvent
import com.plezha.achi.shared.ui.list.achievementlist.AchievementListViewModel
import com.plezha.achi.shared.ui.list.achievementlist.AchievementsScreen
import com.plezha.achi.shared.ui.list.packlist.AchievementPackList
import com.plezha.achi.shared.ui.list.packlist.AchievementPackListViewModel
import com.plezha.achi.shared.ui.debug.DebugPanelScreen
import com.plezha.achi.shared.ui.common.UiText
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
    // Shared reference to the active pack ViewModel (either create or edit mode)
    // EditAchievementRoute reads this to know which ViewModel to update
    val activePackViewModel = remember { mutableStateOf(createAchievementPackViewModel) }

    // Cache for edit pack ViewModels — hoisted to entryProvider scope so they survive
    // entry composition disposal during navigation (e.g. EditPackRoute → EditAchievementRoute → back)
    val editPackVmCache = remember { mutableMapOf<String, CreateAchievementPackViewModel>() }

    // Incremented after a pack edit completes — used as a remember key to force
    // AchievementListViewModel recreation so it reloads fresh data from the server
    var editVersion by remember { mutableStateOf(0) }

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
        activePackViewModel.value = createAchievementPackViewModel

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
            createAchievementPackViewModel.messageFlow.collectLatest { uiText ->
                snackbarHostState.showSnackbar(uiText.resolve())
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

    // Edit Achievement screen (part of pack creation/edit flow)
    entry<EditAchievementRoute> { route ->
        val currentPackViewModel = activePackViewModel.value
        val achievementData = currentPackViewModel.getAchievementAtIndex(route.achievementIndex)
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
                        currentPackViewModel.updateAchievementAtIndex(
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

    // Edit Pack screen (reuses CreateAchievementPackScreen in edit mode)
    entry<EditPackRoute> { route ->
        // Use the hoisted cache so the ViewModel survives navigation to EditAchievementRoute and back
        val editPackViewModel = editPackVmCache.getOrPut(route.packId) {
            CreateAchievementPackViewModel(
                repository = appModule.achievementPackRepository,
                userRepository = appModule.userRepository,
                achievementRepository = appModule.achievementRepository,
                editPackId = route.packId
            ).apply {
                loadExistingPack(route.packId)
            }
        }

        activePackViewModel.value = editPackViewModel

        LaunchedEffect(Unit) {
            editPackViewModel.navigationFlow.collectLatest { event ->
                when (event) {
                    is CreatePackNavigationEvent.NavigateBack -> {
                        editPackVmCache.remove(route.packId)
                        editVersion+=1
                        backStack.popBack()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            editPackViewModel.messageFlow.collectLatest { uiText ->
                snackbarHostState.showSnackbar(uiText.resolve())
            }
        }

        CreateAchievementPackScreen(
            createAchievementPackViewModel = editPackViewModel,
            onBackClicked = {
                editPackVmCache.remove(route.packId)
                backStack.popBack()
            },
            onAchievementClick = { index ->
                backStack.add(EditAchievementRoute(index))
            }
        )
    }

    // Achievement Pack List screen
    entry<AchievementPackListRoute> {
        val achievementPackListViewModel = remember(appModule) {
            AchievementPackListViewModel(
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository,
                achievementRepository = appModule.achievementRepository,
                achievementPackRepository = appModule.achievementPackRepository
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
        // editVersion is included so that after a pack edit completes (version incremented),
        // a fresh ViewModel is created and loadAchievementsByPackId re-fetches from the server.
        val achievementListViewModel = remember(route.id, appModule, editVersion) {
            AchievementListViewModel(
                achievementRepository = appModule.achievementRepository,
                achievementPackRepository = appModule.achievementPackRepository,
                userRepository = appModule.userRepository,
                authRepository = appModule.authRepository
            ).apply {
                loadAchievementsByPackId(route.id)
            }
        }

        LaunchedEffect(Unit) {
            achievementListViewModel.messageFlow.collectLatest { uiText ->
                snackbarHostState.showSnackbar(uiText.resolve())
            }
        }

        LaunchedEffect(Unit) {
            achievementListViewModel.navigationFlow.collectLatest { event ->
                when (event) {
                    is AchievementListNavigationEvent.NavigateToCopy -> {
                        backStack.add(AchievementListRoute(event.packId))
                    }
                }
            }
        }

        AchievementsScreen(
            achievementListViewModel = achievementListViewModel,
            onAchievementClick = { achievement ->
                backStack.add(AchievementRoute(achievement.id))
            },
            onBackClicked = backStack::popBack,
            onRetry = { achievementListViewModel.loadAchievementsByPackId(route.id) },
            onEditPack = {
                backStack.add(EditPackRoute(route.id))
            },
            onDeletePack = {
                achievementListViewModel.deletePack {
                    backStack.popBack()
                }
            },
            onNotOwnerAction = {
                achievementListViewModel.showNotOwnerMessage()
            },
            onCopyPack = {
                achievementListViewModel.copyPack()
            }
        )
    }

    // Achievement Details screen
    entry<AchievementRoute> { route ->
        val viewModel = remember(route.id, appModule) {
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
        val profileViewModel = remember(appModule) {
            ProfileViewModel(appModule.authRepository)
        }
        
        LaunchedEffect(Unit) {
            profileViewModel.messageFlow.collectLatest { message ->
                val resolvedMessage = message.resolve()
                snackbarHostState.showSnackbar(resolvedMessage)
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
        val profileViewModel = remember(appModule) {
            ProfileViewModel(appModule.authRepository)
        }

        LaunchedEffect(Unit) {
            profileViewModel.messageFlow.collectLatest { uiText ->
                snackbarHostState.showSnackbar(uiText.resolve())
            }
        }

        DebugPanelScreen(
            onBackClicked = backStack::popBack,
            onDebugLogin = profileViewModel::onDebugLogin
        )
    }
}
