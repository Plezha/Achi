package com.plezha.achi.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

private const val TAG = "HTTP Client"

actual val httpClientEngine: HttpClientEngine
    get() = OkHttp.create()
//        Log.v(TAG,"OkHttp init")
//        install(Logging) {
//            logger = object: io.ktor.client.plugins.logging.Logger {
//                override fun log(message: String) {
//                    Log.v(TAG, message)
//                }
//            }
//            level = LogLevel.ALL
//        }
//        engine {
//            config {
//                retryOnConnectionFailure(true)
//                connectTimeout(5, TimeUnit.SECONDS)
//                readTimeout(10, TimeUnit.SECONDS)
//                writeTimeout(10, TimeUnit.SECONDS)
//            }
//        }
//    }