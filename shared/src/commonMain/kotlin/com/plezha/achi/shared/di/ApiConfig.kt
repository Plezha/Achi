package com.plezha.achi.shared.di

import com.plezha.achi.shared.data.network.infrastructure.ApiClient

/**
 * Toggle to use localhost instead of the server URL from OpenAPI spec.
 * - false: Use server URL from OpenAPI spec (ngrok)
 * - true: Use localhost (for Android emulator)
 */

object ApiConfig {
    private const val LOCALHOST_ANDROID_EMULATOR = "http://10.0.2.2:8000"
    
    val baseUrl: String = ApiClient.BASE_URL
}
