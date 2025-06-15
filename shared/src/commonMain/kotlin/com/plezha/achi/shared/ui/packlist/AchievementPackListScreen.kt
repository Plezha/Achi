package com.plezha.achi.shared.ui.packlist

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.img
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.data.AchievementPack
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.painterResource


@Composable
fun AchievementPackList(
    achievementPackListViewModel: AchievementPackListViewModel,
    modifier: Modifier = Modifier,
    onPackClick: (String) -> Unit
) {
    val achievementPacks by achievementPackListViewModel.achievementPacks.collectAsState()

    Column(
        modifier = modifier
    ) {
        TitleBar(modifier = Modifier.fillMaxWidth(), "Achievement Packs")
        LazyColumn {
            items(achievementPacks) { achievementPack ->
                AchievementPackCard(
                    achievementPack,
                    onPackClick
                )
                if (achievementPacks.last() != achievementPack) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

}


@Composable
private fun AchievementPackCard(
    achievementPack: AchievementPack,
    onPackClick: (String) -> Unit
) {
    ElevatedCard(onClick = { onPackClick(achievementPack.name) } ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            Image(
                painter = painterResource(Res.drawable.img),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = achievementPack.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${achievementPack.count} achievements",
                    style = MaterialTheme.typography.bodyMedium
                    
                )
            }
        }
    }
}
