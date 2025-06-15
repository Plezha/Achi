package com.plezha.achi.shared.data


data class Achievement(
    val title: String,
    val shortDescription: String,
    val longDescription: String? = null,
    val steps: List<AchievementStep>,
    val stepsDone: Int,
) {
    val currentStep = if (stepsDone < steps.size) steps[stepsDone] else null
    val currentStepProgress = currentStep?.progress
    val isDone = currentStep == null
}

data class AchievementStep(
    val description: String,
    val progress: StepProgress? = null
)

data class StepProgress(
    val stepsDone: Int = 0,
    val totalSteps: Int
)


val achievementExample = Achievement(
    title = "Achievement title",
    shortDescription = "This is an achievement description text which is not really short but also is not too long",
    steps = listOf(
        AchievementStep("Step 1"),
        AchievementStep("Step 2"),
        AchievementStep("Step 3", progress = StepProgress(3, 7)),
        AchievementStep("Step 4"),
        AchievementStep("Step 5"),
    ),
    stepsDone = 2
)