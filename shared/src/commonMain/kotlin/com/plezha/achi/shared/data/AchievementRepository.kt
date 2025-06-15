package com.plezha.achi.shared.data

interface AchievementRepository {
    suspend fun getAchievements(packId: String): List<Achievement>
    suspend fun getAchievementById(id: String): Achievement
}

class MockAchievementRepository : AchievementRepository {
    override suspend fun getAchievements(packId: String): List<Achievement> {
        return achievements
    }

    override suspend fun getAchievementById(id: String): Achievement {
        return achievements.first { it.title == id }
    }

}

private val achievements = listOf(
    Achievement(
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
        stepsDone = 0
    ),
    Achievement(
        title = "Тарасов",
        shortDescription = "аэвм)",
        steps = listOf(
            AchievementStep(description = "Тарасов принял 1 цикл"),
            AchievementStep(description = "дед наконец принял 2 цикл"),
            AchievementStep(
                description = "Прочитать билеты (надеюсь их 77)",
                progress = StepProgress(0, 77)
            ),
            AchievementStep(description = "дед не отправил на комсу"),
        ),
        stepsDone = 0
    ),
    Achievement(
        title = "Цыган",
        shortDescription = ".. и его трансы",
        steps = listOf(
            AchievementStep(description = "Лаба по lex принята"),
            AchievementStep(description = "Лаба по yacc принята"),
            AchievementStep(description = "Индивидуальное задание принято"),
            AchievementStep(description = "В зачётке есть зачёт"),
        ),
        stepsDone = 0
    ),
    Achievement(
        title = "Схемач",
        shortDescription = "ну схемач и схемач чего бухтеть то",
        steps = listOf(
            AchievementStep(description = "В зачётке есть зачёт"),
        ),
        stepsDone = 0
    ),
)