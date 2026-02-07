package com.plezha.achi.shared.data.model


data class Achievement(
    val id: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String? = null,
    val steps: List<AchievementStep>,
    val previewImageUrl: String? = null,
    val imageUrl: String? = null
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

/** Overall progress across all achievements (0.0 to 1.0) */
fun List<Achievement>.overallProgress(): Float =
    if (isEmpty()) 0f else sumOf { it.progress }.toFloat() / size

/** Number of fully completed achievements */
fun List<Achievement>.completedCount(): Int =
    count { it.isDone }

/** Whether every achievement in the list is completed */
fun List<Achievement>.isAllCompleted(): Boolean =
    isNotEmpty() && all { it.isDone }
