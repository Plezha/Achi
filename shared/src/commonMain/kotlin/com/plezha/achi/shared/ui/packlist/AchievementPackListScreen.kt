package com.plezha.achi.shared.ui.packlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.ui.achievmentlist.AchievementCard
import com.plezha.achi.shared.ui.common.TitleBar


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
        TitleBar(
            text = "Achievement Packs",
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn {
            items(
                items = achievementPacks,
                key = { it.name }
            ) { achievementPack ->
                AchievementPackCard(
                    achievementPack = achievementPack,
                    onPackClick = onPackClick
                )
            }
        }
    }

}


@Composable
private fun AchievementPackCard(
    achievementPack: AchievementPack,
    onPackClick: (String) -> Unit
) {
    AchievementCard(
        title = achievementPack.name,
        subtitle = "${achievementPack.count} achievements",
        onClick = { onPackClick(achievementPack.name) },
        modifier = Modifier.fillMaxWidth()
    )
}
