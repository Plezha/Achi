package com.plezha.achi.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual val httpClientEngine: HttpClientEngine
    get() = CIO.create()