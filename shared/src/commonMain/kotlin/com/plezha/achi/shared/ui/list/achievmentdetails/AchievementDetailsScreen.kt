package com.plezha.achi.shared.ui.list.achievmentdetails

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.img
import androidx.annotation.FloatRange
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.data.achievementExample
import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementStep
import com.plezha.achi.shared.ui.common.PreviewWrapper
import com.plezha.achi.shared.ui.common.TitleBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt


@Composable
fun AchievementDetailsScreen(
    viewModel: AchievementDetailsViewModel,
    onBackClicked: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()
    if (uiState.value.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.value.achievement != null) {
        AchievementDetailsScreen(
            achievement = uiState.value.achievement!!,
            onStepProgressIncreased = viewModel::increaseStepProgress,
            onStepCompleted = {
                viewModel.setStepCompleted(it, true)
            },
            onStepProgressReset = {
                viewModel.setStepCompleted(it, false)
            },
            onBackClicked = onBackClicked,
        )
    }
}

@Composable
private fun AchievementDetailsScreen(
    achievement: Achievement,
    onStepCompleted: (AchievementStep) -> Unit,
    onStepProgressReset: (AchievementStep) -> Unit,
    onStepProgressIncreased: (AchievementStep) -> Unit,
    onBackClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(
            modifier = Modifier.fillMaxWidth(),
            text = "Achievement Details",
            onBackClicked = onBackClicked
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            item {
                Image(
                    painter = painterResource(Res.drawable.img),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1.7f)
                )
            }
            item {
                Progress(
                    progress = achievement.progress.toFloat()
                )
            }
            item {
                AchievementDetails(achievement)
                Text(
                    text = "Steps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            items(
                items = achievement.steps,
            ) { step ->
                val stepModifier = remember {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                }
                if (step.progress.substepsAmount == 1) {
                    SimpleStep(
                        step = step,
                        modifier = stepModifier.padding(vertical = 12.dp),
                        onStepCompleted = onStepCompleted,
                        onStepProgressReset = onStepProgressReset
                    )
                } else {
                    IncrementalStep(
                        step = step,
                        onStepProgressIncreased = onStepProgressIncreased,
                        modifier = stepModifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncrementalStep(
    step: AchievementStep,
    onStepProgressIncreased: (AchievementStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Current: ${step.progress.substepsDone}/${step.progress.substepsAmount}",
                style = MaterialTheme.typography.bodySmall.let {
                    it.copy(color = it.color.copy(alpha = 0.75f))
                }
            )
        }
        FilledTonalButton(
            onClick = { onStepProgressIncreased(step) },
            enabled = step.progress.substepsDone < step.progress.substepsAmount,
        ) {
            Text("+1", maxLines = 1)
        }
    }
}

@Composable
private fun SimpleStep(
    step: AchievementStep,
    onStepCompleted: (AchievementStep) -> Unit,
    onStepProgressReset: (AchievementStep) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = step.description,
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Checkbox(
                checked = step.isDone,
                onCheckedChange = {
                    if (it) {
                        onStepCompleted(step)
                    } else {
                        onStepProgressReset(step)
                    }
                }
            )
        }
    }
}

@Composable
private fun AchievementDetails(achievement: Achievement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = achievement.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = achievement.longDescription ?: achievement.shortDescription,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun Progress(
    @FloatRange(0.0, 1.0) progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress"
            )
            Text(
                text = "${(progress*100f).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium.let {
                    it.copy(color = it.color.copy(alpha = 0.75f))
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            finishedListener = {  }
        )

        LinearProgressIndicator(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth(),
            progress = { animatedProgress },
            drawStopIndicator = { },
            gapSize = 0.dp
        )
    }
}

@Composable
private fun StepProgressIndicator(
    stepsDone: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    currentStepProgress: Float = 0f,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    circleSize: Dp = 8.dp,
    lineHeight: Dp = 4.dp
) {
    require(totalSteps > 0)
    val coercedStepsDone = stepsDone.coerceIn(0..totalSteps)
    val coercedCurrentStepProgress = currentStepProgress.coerceIn(0f..1f)

    val animatedProgress by animateFloatAsState(
        targetValue = (coercedStepsDone + coercedCurrentStepProgress) / totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        finishedListener = {  }
    )

    Canvas(modifier = modifier.height(circleSize)) {
        val width = size.width
        val height = size.height
        val circleSizePx = circleSize.toPx()
        val lineHeightPx = lineHeight.toPx()
        val centerY = height / 2

        val startX = circleSizePx / 2
        val endX = width - circleSizePx / 2
        val totalLineLength = endX - startX
        val interval = totalLineLength / totalSteps

        // inactive line
        drawLine(
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            color = inactiveColor,
            strokeWidth = lineHeightPx,
            cap = StrokeCap.Butt
        )

        // active line
        val progressLength = totalLineLength * animatedProgress
        if (progressLength > 0) {
            drawLine(
                start = Offset(startX, centerY),
                end = Offset(startX + progressLength, centerY),
                color = activeColor,
                strokeWidth = lineHeightPx,
                cap = StrokeCap.Round
            )
        }

        // circles
        for (i in 0..totalSteps) {
            if (i == 0 || i == totalSteps) continue

            val activationThreshold = i.toFloat() / totalSteps.toFloat()
            val color =
                if (animatedProgress >= activationThreshold - 1e-7) activeColor else inactiveColor
            val centerX = startX + interval * i

            drawCircle(
                color = color,
                radius = circleSizePx / 2,
                center = Offset(centerX, centerY)
            )
        }
    }
}


@Preview
@Composable
private fun AchievementDetailsScreenPreview() {
    PreviewWrapper {
        AchievementDetailsScreen(
            achievement = achievementExample,
            onStepCompleted = { },
            onStepProgressReset = { },
            onStepProgressIncreased = { },
        ) { }
    }
}

@Preview
@Composable
private fun ProgressPreview() {
    PreviewWrapper {
        Progress(0.6f)
    }
}

@Preview
@Composable
private fun StepsProgressIndicatorPreview() {
    PreviewWrapper {
        StepProgressIndicator(
            stepsDone = 2,
            totalSteps = 5,
            currentStepProgress = 0.4f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}