package com.plezha.achi.shared

/**
 * Reads the debug flag injected by webpack's BannerPlugin (see webpack.config.d/debug-flag.js).
 * Returns true for development builds, false for production builds.
 */
@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.__ACHI_IS_DEBUG__ === true")
private external fun jsIsDebug(): Boolean

actual val isDebug: Boolean = jsIsDebug()
