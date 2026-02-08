package com.plezha.achi.shared.di

import com.plezha.achi.shared.data.network.infrastructure.ApiClient
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * API host configuration.
 * The base URL can be changed at runtime via the debug panel,
 * which triggers AppModule recreation.
 * The selected host is persisted via [Settings] so it survives app restarts.
 */
object ApiConfig {
    const val LOCALHOST_ANDROID_EMULATOR = "http://10.0.2.2:8000"
    const val PROD = "http://31.44.9.104:8000"
    val DEFAULT: String = ApiClient.BASE_URL

    private const val KEY_BASE_URL = "debug_base_url"
    private val settings: Settings = Settings()

    private val _baseUrl = MutableStateFlow(settings.getString(KEY_BASE_URL, PROD))
    val baseUrlFlow: StateFlow<String> = _baseUrl.asStateFlow()

    /** Current base URL value. */
    val baseUrl: String get() = _baseUrl.value

    /** Update the base URL, persisting the choice. Triggers AppModule recreation when observed. */
    fun setBaseUrl(url: String) {
        settings.putString(KEY_BASE_URL, url)
        _baseUrl.value = url
    }

    /** All preset host options for the debug panel. */
    val presets: List<Pair<String, String>> = listOf(
        "Production" to PROD,
        "Localhost (Android Emulator)" to LOCALHOST_ANDROID_EMULATOR,
        "OpenAPI Default" to DEFAULT
    )
}
