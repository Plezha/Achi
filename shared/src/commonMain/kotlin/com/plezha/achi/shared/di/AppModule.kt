package com.plezha.achi.shared.di

import com.plezha.achi.shared.data.AchievementPackRepository
import com.plezha.achi.shared.data.AchievementPackRepositoryImpl
import com.plezha.achi.shared.data.AchievementRepository
import com.plezha.achi.shared.data.AchievementRepositoryImpl
import com.plezha.achi.shared.data.UserRepository
import com.plezha.achi.shared.data.UserRepositoryImpl
import com.plezha.achi.shared.data.auth.AuthRepository
import com.plezha.achi.shared.data.network.apis.AchievementsApi
import com.plezha.achi.shared.data.network.apis.AuthenticationApi
import com.plezha.achi.shared.data.network.apis.PacksApi
import com.plezha.achi.shared.data.network.apis.UploadApi
import com.plezha.achi.shared.data.network.apis.UserCollectionApi
import com.plezha.achi.shared.data.network.apis.UserProgressApi
import com.plezha.achi.shared.data.network.apis.UsersApi
import io.ktor.client.engine.HttpClientEngine

// API Base URL for testing (Android emulator uses 10.0.2.2 to access host's localhost)
private const val API_BASE_URL = "http://10.0.2.2:8000"

/**
 * Manual dependency injection container.
 * Creates and holds all API clients and repositories.
 */
class AppModule(httpClientEngine: HttpClientEngine) {
    
    // API Clients
    val usersApi = UsersApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val authApi = AuthenticationApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val achievementsApi = AchievementsApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val packsApi = PacksApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val uploadApi = UploadApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val userCollectionApi = UserCollectionApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    val userProgressApi = UserProgressApi(baseUrl = API_BASE_URL, httpClientEngine = httpClientEngine)
    
    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authApi = authApi,
            usersApi = usersApi,
            achievementsApi = achievementsApi,
            packsApi = packsApi,
            uploadApi = uploadApi,
            userCollectionApi = userCollectionApi,
            userProgressApi = userProgressApi
        )
    }
    
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            userCollectionApi = userCollectionApi,
            userProgressApi = userProgressApi
        )
    }
    
    val achievementRepository: AchievementRepository by lazy {
        AchievementRepositoryImpl(achievementsApi = achievementsApi)
    }
    
    val achievementPackRepository: AchievementPackRepository by lazy {
        AchievementPackRepositoryImpl(
            achievementsApi = achievementsApi,
            packsApi = packsApi,
            uploadApi = uploadApi
        )
    }
}
