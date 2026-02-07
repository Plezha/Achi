package com.plezha.achi.shared.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BottomNavigationBar(
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
                is ProfileRoute, is SettingsRoute, is DebugPanelRoute -> 
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
