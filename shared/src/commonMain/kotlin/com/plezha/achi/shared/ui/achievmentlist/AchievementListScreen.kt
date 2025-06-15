package com.plezha.achi.shared.ui.achievmentlist

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.img
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.data.Achievement
import com.plezha.achi.shared.data.achievementExample
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun AchievementsScreen(
    achievementListViewModel: AchievementListViewModel,
    onAchievementClick: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    val achievements by achievementListViewModel.achievements.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleBar(Modifier.fillMaxWidth(), "Achievements", onBackClicked)

        LazyColumn {
            items(achievements) { achievement ->
                AchievementCard(achievement, onAchievementClick = onAchievementClick)
                if (achievements.last() != achievement) Spacer(Modifier.height(8.dp))
            }
        }

    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    onAchievementClick: (String) -> Unit
) {
    val cardCorners = remember { RoundedCornerShape(8.dp) }
    ElevatedCard(
        onClick = { onAchievementClick(achievement.title) },
        modifier = Modifier.height(80.dp),
        shape = cardCorners
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Image(
                    painter = painterResource(Res.drawable.img),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(shape = cardCorners)
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        text = achievement.shortDescription,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AchievementCardPreview() {
    AchievementCard(achievementExample) {}
}

@Preview
@Composable
fun AchievementCardWithLargeDescriptionPreview() {
    AchievementCard(achievementExample) {}
}