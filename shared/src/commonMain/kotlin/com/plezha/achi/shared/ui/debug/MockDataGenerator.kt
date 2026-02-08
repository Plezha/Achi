package com.plezha.achi.shared.ui.debug

import com.plezha.achi.shared.data.network.check
import com.plezha.achi.shared.data.network.models.AchievementCreateBody
import com.plezha.achi.shared.data.network.models.AchievementPackCreateBody
import com.plezha.achi.shared.data.network.models.AchievementStepCreate
import com.plezha.achi.shared.di.AppModule

private const val MOCK_IMAGE_URL =
    "https://drive.google.com/uc?export=view&id=1qM_rQtiKaKD03ReLIASklvztAusZBLTz"

/**
 * Creates mock achievement packs with realistic data for Google Play screenshots.
 * Requires the user to be logged in.
 */
suspend fun populateMockData(appModule: AppModule) {
    val achievementsApi = appModule.achievementsApi
    val packsApi = appModule.packsApi
    val userCollectionApi = appModule.userCollectionApi

    // --- Pack 1: 30-Day Fitness Challenge ---
    val fitnessPack = createPackWithAchievements(
        packName = "30-Day Fitness Challenge",
        achievements = listOf(
            AchievementCreateBody(
                title = "Morning Run",
                shortDescription = "Run every morning for a week",
                longDescription = "Start your day with energy! Complete a morning run each day for 7 days straight. Any distance counts — the goal is consistency.",
                steps = (1..7).map { AchievementStepCreate(description = "Day $it") },
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Push-up Master",
                shortDescription = "Build up to 50 push-ups",
                longDescription = "Progressive push-up training. Start small and work your way up to 50 push-ups in a single set.",
                steps = listOf(
                    AchievementStepCreate(description = "10 push-ups in a row"),
                    AchievementStepCreate(description = "25 push-ups in a row"),
                    AchievementStepCreate(description = "50 push-ups in a row")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Yoga Stretch",
                shortDescription = "Complete a full yoga session",
                longDescription = "Take time for flexibility and mindfulness. Complete at least one full yoga session of 30 minutes or more.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Stay Hydrated",
                shortDescription = "Drink 8 glasses of water daily for a week",
                longDescription = "Hydration is key to health and performance. Track your daily water intake — aim for 8 glasses each day.",
                steps = (1..7).map {
                    AchievementStepCreate(description = "Day $it", substepsAmount = 8)
                },
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // --- Pack 2: Book Club 2026 ---
    val bookPack = createPackWithAchievements(
        packName = "Book Club 2026",
        achievements = listOf(
            AchievementCreateBody(
                title = "Read 'The Great Gatsby'",
                shortDescription = "F. Scott Fitzgerald's classic",
                longDescription = "Dive into the roaring twenties with Jay Gatsby. A tale of ambition, love, and the American Dream.",
                steps = listOf(
                    AchievementStepCreate(description = "Start reading"),
                    AchievementStepCreate(description = "Reach halfway"),
                    AchievementStepCreate(description = "Finish the book")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Read '1984'",
                shortDescription = "George Orwell's dystopian masterpiece",
                longDescription = "Explore a world of surveillance and control. One of the most influential novels of the 20th century.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Read 'To Kill a Mockingbird'",
                shortDescription = "Harper Lee's Pulitzer Prize winner",
                longDescription = "A story of racial injustice and childhood innocence in the American South, told through the eyes of Scout Finch.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Read 'The Hobbit'",
                shortDescription = "J.R.R. Tolkien's adventure",
                longDescription = "Follow Bilbo Baggins on an unexpected journey through Middle-earth. The book that started it all.",
                steps = listOf(
                    AchievementStepCreate(description = "An Unexpected Party"),
                    AchievementStepCreate(description = "Riddles in the Dark"),
                    AchievementStepCreate(description = "The Last Stage")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Read 'Pride and Prejudice'",
                shortDescription = "Jane Austen's beloved romance",
                longDescription = "Wit, manners, and marriage in Regency-era England. Follow Elizabeth Bennet and Mr. Darcy's unforgettable story.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // --- Pack 3: Travel Bucket List ---
    val travelPack = createPackWithAchievements(
        packName = "Travel Bucket List",
        achievements = listOf(
            AchievementCreateBody(
                title = "Visit Paris",
                shortDescription = "The City of Light awaits",
                longDescription = "Experience the magic of Paris — from the Eiffel Tower to cozy cafés on the Seine.",
                steps = listOf(
                    AchievementStepCreate(description = "Book flights"),
                    AchievementStepCreate(description = "Visit the Eiffel Tower"),
                    AchievementStepCreate(description = "Try authentic croissants"),
                    AchievementStepCreate(description = "Walk along the Seine")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "See the Northern Lights",
                shortDescription = "Witness the aurora borealis",
                longDescription = "One of nature's most spectacular displays. Travel to Scandinavia, Iceland, or Canada to see the sky dance with color.",
                steps = listOf(
                    AchievementStepCreate(description = "Choose a destination"),
                    AchievementStepCreate(description = "Book the trip"),
                    AchievementStepCreate(description = "Watch the lights")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Road Trip Adventure",
                shortDescription = "Hit the open road",
                longDescription = "Plan an epic road trip with friends or solo. Discover hidden gems, scenic routes, and roadside diners along the way.",
                steps = listOf(
                    AchievementStepCreate(description = "Plan the route"),
                    AchievementStepCreate(description = "Pack the car"),
                    AchievementStepCreate(description = "Hit the road"),
                    AchievementStepCreate(description = "Take amazing photos")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Beach Vacation",
                shortDescription = "Relax by the ocean",
                longDescription = "Sometimes you just need sun, sand, and waves. Pick your perfect beach and unwind.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // Add all packs to user's collection
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(fitnessPack).check()
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(bookPack).check()
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(travelPack).check()

    // Reload packs so they appear in the UI
    appModule.userRepository.loadUserPacks()
}

/**
 * Creates mock achievement packs with Russian data for Google Play screenshots.
 * Requires the user to be logged in.
 */
suspend fun populateMockDataRu(appModule: AppModule) {
    val userCollectionApi = appModule.userCollectionApi

    // --- Набор 1: Фитнес-челлендж на 30 дней ---
    val fitnessPack = createPackWithAchievements(
        packName = "Фитнес-челлендж на 30 дней",
        achievements = listOf(
            AchievementCreateBody(
                title = "Утренняя пробежка",
                shortDescription = "Бегай каждое утро целую неделю",
                longDescription = "Начни день с энергией! Пробегись каждое утро 7 дней подряд. Любая дистанция подойдёт — главное регулярность.",
                steps = (1..7).map { AchievementStepCreate(description = "День $it") },
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Мастер отжиманий",
                shortDescription = "Дойди до 50 отжиманий",
                longDescription = "Прогрессивная тренировка отжиманий. Начни с малого и дойди до 50 отжиманий за один подход.",
                steps = listOf(
                    AchievementStepCreate(description = "10 отжиманий подряд"),
                    AchievementStepCreate(description = "25 отжиманий подряд"),
                    AchievementStepCreate(description = "50 отжиманий подряд")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Йога-растяжка",
                shortDescription = "Пройди полноценное занятие йогой",
                longDescription = "Удели время гибкости и осознанности. Пройди хотя бы одно полноценное занятие йогой от 30 минут.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Водный баланс",
                shortDescription = "Пей 8 стаканов воды в день целую неделю",
                longDescription = "Гидратация — ключ к здоровью и продуктивности. Отслеживай потребление воды — 8 стаканов каждый день.",
                steps = (1..7).map {
                    AchievementStepCreate(description = "День $it", substepsAmount = 8)
                },
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // --- Набор 2: Книжный клуб 2026 ---
    val bookPack = createPackWithAchievements(
        packName = "Книжный клуб 2026",
        achievements = listOf(
            AchievementCreateBody(
                title = "Прочитать «Великий Гэтсби»",
                shortDescription = "Классика Ф. Скотта Фицджеральда",
                longDescription = "Погрузись в ревущие двадцатые вместе с Джеем Гэтсби. История амбиций, любви и американской мечты.",
                steps = listOf(
                    AchievementStepCreate(description = "Начать читать"),
                    AchievementStepCreate(description = "Дойти до середины"),
                    AchievementStepCreate(description = "Дочитать книгу")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Прочитать «1984»",
                shortDescription = "Антиутопия Джорджа Оруэлла",
                longDescription = "Исследуй мир тотальной слежки и контроля. Один из самых влиятельных романов XX века.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Прочитать «Убить пересмешника»",
                shortDescription = "Пулитцеровская премия Харпер Ли",
                longDescription = "История расовой несправедливости и детской невинности на юге Америки, рассказанная глазами Скаут Финч.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Прочитать «Хоббит»",
                shortDescription = "Приключение Дж. Р. Р. Толкина",
                longDescription = "Отправься с Бильбо Бэггинсом в неожиданное путешествие по Средиземью. Книга, с которой всё началось.",
                steps = listOf(
                    AchievementStepCreate(description = "Нежданное путешествие"),
                    AchievementStepCreate(description = "Загадки в темноте"),
                    AchievementStepCreate(description = "Обратный путь")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Прочитать «Гордость и предубеждение»",
                shortDescription = "Любимый роман Джейн Остин",
                longDescription = "Остроумие, манеры и браки в Англии эпохи Регентства. История Элизабет Беннет и мистера Дарси.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // --- Набор 3: Список путешествий мечты ---
    val travelPack = createPackWithAchievements(
        packName = "Путешествия мечты",
        achievements = listOf(
            AchievementCreateBody(
                title = "Побывать в Париже",
                shortDescription = "Город света ждёт",
                longDescription = "Почувствуй магию Парижа — от Эйфелевой башни до уютных кафе на берегу Сены.",
                steps = listOf(
                    AchievementStepCreate(description = "Забронировать билеты"),
                    AchievementStepCreate(description = "Посетить Эйфелеву башню"),
                    AchievementStepCreate(description = "Попробовать настоящие круассаны"),
                    AchievementStepCreate(description = "Прогуляться вдоль Сены")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Увидеть северное сияние",
                shortDescription = "Стать свидетелем авроры",
                longDescription = "Одно из самых впечатляющих зрелищ природы. Отправься в Скандинавию, Исландию или Канаду, чтобы увидеть танец красок в небе.",
                steps = listOf(
                    AchievementStepCreate(description = "Выбрать направление"),
                    AchievementStepCreate(description = "Забронировать поездку"),
                    AchievementStepCreate(description = "Увидеть сияние")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Автопутешествие",
                shortDescription = "В путь по открытой дороге",
                longDescription = "Спланируй эпическое автопутешествие с друзьями или в одиночку. Открывай скрытые жемчужины, живописные маршруты и придорожные кафе.",
                steps = listOf(
                    AchievementStepCreate(description = "Спланировать маршрут"),
                    AchievementStepCreate(description = "Собрать машину"),
                    AchievementStepCreate(description = "Выехать в путь"),
                    AchievementStepCreate(description = "Сделать крутые фото")
                ),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            ),
            AchievementCreateBody(
                title = "Пляжный отдых",
                shortDescription = "Отдохнуть у океана",
                longDescription = "Иногда нужны просто солнце, песок и волны. Выбери идеальный пляж и расслабься.",
                steps = emptyList(),
                previewImageUrl = MOCK_IMAGE_URL,
                imageUrl = MOCK_IMAGE_URL
            )
        ),
        appModule = appModule
    )

    // Add all packs to user's collection
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(fitnessPack).check()
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(bookPack).check()
    userCollectionApi.addPackToCollectionUserPacksPackCodePost(travelPack).check()

    // Reload packs so they appear in the UI
    appModule.userRepository.loadUserPacks()
}

/**
 * Creates achievements and a pack containing them.
 * @return The pack code (used to add to user's collection)
 */
private suspend fun createPackWithAchievements(
    packName: String,
    achievements: List<AchievementCreateBody>,
    appModule: AppModule
): String {
    val achievementsApi = appModule.achievementsApi
    val packsApi = appModule.packsApi

    val achievementIds = achievements.map { achievement ->
        val response = achievementsApi.createAchievementAchievementsPost(achievement)
        response.check()
        response.body().id
    }

    val packResponse = packsApi.createPackPacksPost(
        AchievementPackCreateBody(
            name = packName,
            achievementIds = achievementIds,
            previewImageUrl = MOCK_IMAGE_URL
        )
    )
    packResponse.check()
    return packResponse.body().code
}
