package com.plezha.achi.shared.ui.achievmentdetails

import achi.shared.generated.resources.Res
import achi.shared.generated.resources.img
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plezha.achi.shared.data.Achievement
import com.plezha.achi.shared.data.AchievementStep
import com.plezha.achi.shared.data.achievementExample
import com.plezha.achi.shared.ui.common.TitleBar
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


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
            onStepCompleted = { viewModel.incrementAchievementProgress() },
            onStepPartlyCompleted = { viewModel.incrementStepProgress() },
            onBackClicked = onBackClicked
        )
        SupervisorJob()
    }
}

@Composable
private fun AchievementDetailsScreen(
    achievement: Achievement,
    onStepCompleted: () -> Unit,
    onStepPartlyCompleted: () -> Unit,
    onBackClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleBar(
            modifier = Modifier.fillMaxWidth(),
            text = achievement.title,
            onBackClicked = onBackClicked
        )
        Image(
            painter = painterResource(Res.drawable.img),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 8.dp, start = 32.dp, end = 32.dp, bottom = 8.dp)
                .aspectRatio(1.2f)
                .clip(shape = RoundedCornerShape(8.dp))
        )
        Text(
            text = "\t" + (achievement.longDescription ?: achievement.shortDescription),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge
        )
        if (achievement.isDone) {
            Text("Achievement Done")
        } else {
            val currentStep = achievement.currentStep!!
            StepDetails(
                achievement = achievement,
                step = currentStep,
                onStepPartlyCompleted = onStepPartlyCompleted,
                onStepCompleted = onStepCompleted
            )
        }
    }
}

@Composable
private fun StepDetails(
    achievement: Achievement,
    step: AchievementStep,
    onStepPartlyCompleted: () -> Unit,
    onStepCompleted: () -> Unit
) {
    val stepDescription =
        if (step.progress != null)
            step.description +
                    " (${step.progress.stepsDone}/${step.progress.totalSteps})"
        else
            step.description
    StepProgressIndicator(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .padding(8.dp),
        stepsDone = achievement.stepsDone,
        totalSteps = achievement.steps.size,
        currentStepProgress =
        if (step.progress != null)
            step.progress.stepsDone.toFloat() / step.progress.totalSteps
        else
            0f
    )
    Text(
        text = stepDescription,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (step.progress != null) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = onStepPartlyCompleted
            ) {
                Text("Next step part")
            }
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            onClick = onStepCompleted
        ) {
            Text("Next step")
        }
    }
}

//@Composable
//fun StepProgressIndicator(
//    stepsDone: Int,
//    totalSteps: Int,
//    modifier: Modifier = Modifier,
//    currentStepProgress: Float = 0f,
//    activeColor: Color = MaterialTheme.colorScheme.primary,
//    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
//    circleSize: Dp = 8.dp,
//    lineHeight: Dp = 4.dp
//) {
//    require(totalSteps > 0)
//    val coercedStepsDone = stepsDone.coerceIn(0..totalSteps)
//    val coercedCurrentStepProgress = currentStepProgress.coerceIn(0f..1f)
//
//    Box(modifier = modifier.height(circleSize)) {
//        LinearProgressIndicator(
//            progress = {
//                (coercedStepsDone.toFloat() + coercedCurrentStepProgress) / totalSteps
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.CenterStart)
//                .height(lineHeight),
//            color = activeColor,
//            strokeCap = StrokeCap.Round
//        )
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            repeat(totalSteps + 1) { index ->
//                val color =
//                    if (index == 0 || index == totalSteps)
//                        Color.Transparent
//                    else if (index <= coercedStepsDone)
//                        activeColor
//                    else
//                        inactiveColor
//                Box(
//                    modifier = Modifier
//                        .size(circleSize)
//                        .background(color, CircleShape)
//                )
//            }
//        }
//    }
//}

@Composable
fun StepProgressIndicator(
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
private fun StepsProgressIndicatorPreview() {
    StepProgressIndicator(
        stepsDone = 2,
        totalSteps = 5,
        currentStepProgress = 0.4f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview()
@Composable
private fun AchievementDetailsScreenPreview() {
    AchievementDetailsScreen(achievementExample, { }, { }) { }
}