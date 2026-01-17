package com.plezha.achi.shared.ui.list.packlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.ui.common.TitleBar
import com.plezha.achi.shared.ui.list.achievementlist.AchievementCard


@Composable
fun AchievementPackList(
    achievementPackListViewModel: AchievementPackListViewModel,
    modifier: Modifier = Modifier,
    onPackClick: (AchievementPack) -> Unit
) {
    val uiState by achievementPackListViewModel.uiState.collectAsState()

    Column(
        modifier = modifier
    ) {
        TitleBar(
            text = "Achievement Packs",
            modifier = Modifier.fillMaxWidth(),
        )
        
        when {
            uiState.isLoading && uiState.packs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            !uiState.isLoggedIn -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please log in to see your achievement packs",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            uiState.packs.isEmpty() && !uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No achievement packs yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add packs using a code in the Add tab",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn {
                    items(
                        items = uiState.packs,
                        key = { it.id }
                    ) { achievementPack ->
                        AchievementPackCard(
                            achievementPack = achievementPack,
                            onPackClick = { onPackClick(achievementPack) }
                        )
                    }
                }
            }
        }
    }

}


@Composable
private fun AchievementPackCard(
    achievementPack: AchievementPack,
    onPackClick: () -> Unit
) {
    AchievementCard(
        title = achievementPack.name,
        subtitle = "${achievementPack.count} achievements",
        onClick = onPackClick,
        modifier = Modifier.fillMaxWidth()
    )
}
