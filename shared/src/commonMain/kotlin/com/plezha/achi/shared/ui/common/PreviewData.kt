package com.plezha.achi.shared.ui.common

import com.plezha.achi.shared.data.model.Achievement
import com.plezha.achi.shared.data.model.AchievementStep
import com.plezha.achi.shared.data.model.StepProgress

/**
 * Sample data for @Preview composables.
 */
val achievementExample = Achievement(
    id = "1",
    title = "САПР",
    shortDescription = "Системный Анализ и Принятие Решений",
    steps = listOf(
        AchievementStep(description = "Первая лаба принята"),
        AchievementStep(description = "Вторая лаба принята"),
        AchievementStep(description = "Третья лаба принята"),
        AchievementStep(description = "Четвертая лаба принята"),
        AchievementStep(description = "Пятая лаба принята"),
        AchievementStep(description = "Шестая лаба принята"),
        AchievementStep(
            description = "Подготовиться хоть чуть чуть к экзу",
            progress = StepProgress(0, 10)
        ),
        AchievementStep(description = "В зачётке зачёт"),
    ),
)
