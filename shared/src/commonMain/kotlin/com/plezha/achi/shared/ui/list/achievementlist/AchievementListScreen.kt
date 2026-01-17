package com.plezha.achi.shared.ui.list.achievementlist

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.img
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.ui.common.achievementExample
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.ui.common.PreviewWrapper
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun AchievementsScreen(
    achievementListViewModel: AchievementListViewModel,
    onAchievementClick: (Achievement) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState by achievementListViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleBar(
            text = uiState.pack?.name ?: "Achievements",
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth(),
        )
        
        // Show pack code for sharing
        uiState.pack?.code?.let { code ->
            PackCodeBanner(code = code)
        }

        LazyColumn {
            items(
                items = uiState.achievements,
                key = { it.title + it.shortDescription }
            ) { achievement ->
                AchievementCard(
                    title = achievement.title,
                    subtitle = achievement.shortDescription,
                    onClick = { onAchievementClick(achievement) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    }
}

@Composable
private fun PackCodeBanner(code: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Share code:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AchievementCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(CardDefaults.cardColors().containerColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.img),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.aligned(Alignment.CenterVertically),
            modifier = Modifier.height(IntrinsicSize.Max)
        ) {
            Text(
                modifier = Modifier,
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier,
                text = subtitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.5f))
            )
        }

    }
}

@Preview
@Composable
private fun AchievementCardPreview() {
    PreviewWrapper {
        AchievementCard(
            achievementExample.title,
            achievementExample.shortDescription,
            {}
        )
    }
}
