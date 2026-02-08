package com.plezha.achi.shared.navigation

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.ic_cup_filled
import achi.shared.generated.resources.ic_list
import achi.shared.generated.resources.ic_plus
import achi.shared.generated.resources.ic_plus_filled_outside
import achi.shared.generated.resources.ic_profile
import achi.shared.generated.resources.ic_profile_filled
import achi.shared.generated.resources.nav_add
import achi.shared.generated.resources.nav_achievements
import achi.shared.generated.resources.nav_profile
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

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

@Serializable
data class EditPackRoute(val packId: String) : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object DebugPanelRoute : NavKey

// Polymorphic serialization configuration for multiplatform support
val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AchievementPackListRoute::class, AchievementPackListRoute.serializer())
            subclass(AddRoute::class, AddRoute.serializer())
            subclass(CreateAchievementPackRoute::class, CreateAchievementPackRoute.serializer())
            subclass(EditAchievementRoute::class, EditAchievementRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(AchievementRoute::class, AchievementRoute.serializer())
            subclass(AchievementListRoute::class, AchievementListRoute.serializer())
            subclass(EditPackRoute::class, EditPackRoute.serializer())
            subclass(SettingsRoute::class, SettingsRoute.serializer())
            subclass(DebugPanelRoute::class, DebugPanelRoute.serializer())
        }
    }
}

// Bottom navigation configuration
data class TopLevelRoute<T : NavKey>(
    val route: T,
    val iconUnselected: DrawableResource,
    val iconSelected: DrawableResource,
    val label: StringResource
)

val topLevelRoutes = listOf(
    TopLevelRoute(AddRoute, Res.drawable.ic_plus, Res.drawable.ic_plus_filled_outside, Res.string.nav_add),
    TopLevelRoute(AchievementPackListRoute, Res.drawable.ic_list, Res.drawable.ic_cup_filled, Res.string.nav_achievements),
    TopLevelRoute(ProfileRoute, Res.drawable.ic_profile, Res.drawable.ic_profile_filled, Res.string.nav_profile),
)
