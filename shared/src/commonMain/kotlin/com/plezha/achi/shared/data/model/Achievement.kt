package com.plezha.achi.shared.data.model


data class Achievement(
    val title: String,
    val shortDescription: String,
    val longDescription: String? = null,
    val steps: List<AchievementStep>,
) {
    val progress
        get() = steps.sumOf { it.progress.progressFloat.toDouble() } / steps.size
    val stepsDone
        get() = steps.count { it.isDone }
    val isDone
        get() = stepsDone == steps.size
}

data class AchievementStep(
    val description: String,
    val progress: StepProgress = StepProgress(),
) {
    val isDone = progress.substepsDone == progress.substepsAmount
    fun asCompleted() = copy(progress = progress.asCompleted())
    fun asNotStarted() = copy(progress = progress.asNotStarted())
}

data class StepProgress(
    val substepsDone: Int = 0,
    val substepsAmount: Int = 1
) {
    init {
        require(substepsDone in 0..substepsAmount)
    }
    val progressFloat = substepsDone.toFloat() / substepsAmount
    val isCompleted = substepsDone == substepsAmount
    fun asCompleted() = copy(substepsDone = substepsAmount)
    fun asNotStarted() = copy(substepsDone = 0)
}


val achievementExample = Achievement(
    title = "Achievement title",
    shortDescription = "This is an achievement description text which is not really short but also is not too long",
    steps = listOf(
        AchievementStep("Step 1", progress = StepProgress(1, 1)),
        AchievementStep("Step 2", progress = StepProgress(1, 1)),
        AchievementStep("Step 3", progress = StepProgress(3, 7)),
        AchievementStep("Step 4"),
        AchievementStep("Step 5"),
    ),
)