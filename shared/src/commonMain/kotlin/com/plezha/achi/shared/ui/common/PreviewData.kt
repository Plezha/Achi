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
        AchievementStep(id = "step-1", description = "Первая лаба принята"),
        AchievementStep(id = "step-2", description = "Вторая лаба принята"),
        AchievementStep(id = "step-3", description = "Третья лаба принята"),
        AchievementStep(id = "step-4", description = "Четвертая лаба принята"),
        AchievementStep(id = "step-5", description = "Пятая лаба принята"),
        AchievementStep(id = "step-6", description = "Шестая лаба принята"),
        AchievementStep(
            id = "step-7",
            description = "Подготовиться хоть чуть чуть к экзу",
            progress = StepProgress(0, 10)
        ),
        AchievementStep(id = "step-8", description = "В зачётке зачёт"),
    ),
)
