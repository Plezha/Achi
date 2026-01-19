package com.plezha.achi.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual val httpClientEngine: HttpClientEngine
    get() = Js.create()