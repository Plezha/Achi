package com.plezha.achi.shared.ui.list.achievementlist

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.*
import achi.shared.generated.resources.ic_check
import achi.shared.generated.resources.img_trophy_lifting
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementPack
import com.plezha.achi.shared.ui.common.PreviewWrapper
import com.plezha.achi.shared.ui.common.TitleBar
import com.plezha.achi.shared.ui.common.achievementExample
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt


@Composable
fun AchievementsScreen(
    achievementListViewModel: AchievementListViewModel,
    onAchievementClick: (Achievement) -> Unit,
    onBackClicked: () -> Unit,
    onRetry: () -> Unit = {},
    onEditPack: () -> Unit = {},
    onDeletePack: () -> Unit = {},
    onNotOwnerAction: () -> Unit = {},
    onCopyPack: () -> Unit = {}
) {
    val uiState by achievementListViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.delete_pack_confirm_title)) },
            text = { Text(stringResource(Res.string.delete_pack_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeletePack()
                    }
                ) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        )
    }

    val disabledTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TitleBar(
            text = uiState.pack?.name ?: stringResource(Res.string.achievement_list_fallback_title),
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth(),
            actions = {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_more_vert),
                            contentDescription = stringResource(Res.string.common_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.common_copy)) },
                            onClick = {
                                menuExpanded = false
                                onCopyPack()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_copy),
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.common_edit),
                                    color = if (uiState.isOwner) {
                                        Color.Unspecified
                                    } else {
                                        disabledTint
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                if (uiState.isOwner) onEditPack() else onNotOwnerAction()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_edit),
                                    contentDescription = null,
                                    tint = if (uiState.isOwner) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        disabledTint
                                    }
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.common_delete),
                                    color = if (uiState.isOwner) {
                                        Color.Unspecified
                                    } else {
                                        disabledTint
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                if (uiState.isOwner) {
                                    showDeleteDialog = true
                                } else {
                                    onNotOwnerAction()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_delete),
                                    contentDescription = null,
                                    tint = if (uiState.isOwner) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        disabledTint
                                    }
                                )
                            }
                        )
                    }
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(Res.string.common_error_generic),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onRetry) {
                            Text(stringResource(Res.string.common_retry))
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Hero header with pack image and progress
                    uiState.pack?.let { pack ->
                        item {
                            PackHeroHeader(
                                pack = pack,
                                overallProgress = uiState.overallProgress,
                                completedCount = uiState.completedCount,
                                totalCount = uiState.achievements.size
                            )
                        }
                    }

                    // Achievement list
                    items(
                        items = uiState.achievements,
                        key = { it.id }
                    ) { achievement ->
                        EnhancedAchievementCard(
                            achievement = achievement,
                            onClick = { onAchievementClick(achievement) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackHeroHeader(
    pack: AchievementPack,
    overallProgress: Float,
    completedCount: Int,
    totalCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
    ) {
        // Background image
        if (pack.previewImageUrl != null) {
            AsyncImage(
                model = pack.previewImageUrl,
                contentDescription = pack.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.img_trophy_lifting),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (overallProgress >= 1.0f) {
                                Color.Green.copy(alpha = 0.5f)
                            } else {
                                Color.Black.copy(alpha = 0.7f)
                            }
                        )
                    )
                )
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Share code badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.common_code_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = pack.code,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Progress section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.achievement_list_completed_count, completedCount, totalCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${(overallProgress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                
                val animatedProgress by animateFloatAsState(
                    targetValue = overallProgress,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = Color.White.copy(alpha = 0.3f),
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun EnhancedAchievementCard(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress indicator or completion checkmark
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isDone) {
                    // Completed: show checkmark
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_check),
                            contentDescription = stringResource(Res.string.achievement_list_completed_cd),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                        )
                    }
                } else {
                    // In progress: show circular progress
                    val animatedProgress by animateFloatAsState(
                        targetValue = achievement.progress.toFloat(),
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    // Progress percentage in center
                    Text(
                        text = "${(achievement.progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Title and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = achievement.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // Steps progress text
                if (achievement.steps.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            Res.string.achievement_list_steps_progress,
                            achievement.stepsDone,
                            achievement.steps.size
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (achievement.isDone)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Keep old AchievementCard for backward compatibility (used in pack list)
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
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.img_trophy_lifting),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview
@Composable
private fun EnhancedAchievementCardPreview() {
    PreviewWrapper {
        EnhancedAchievementCard(
            achievement = achievementExample,
            onClick = {}
        )
    }
}
