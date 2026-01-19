package com.plezha.achi.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val httpClientEngine: HttpClientEngine
    get() = Darwin.create()
