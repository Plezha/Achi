package com.plezha.achi.shared

// WasmJS is currently a secondary dev platform, so treat it as debug.
// For production webpack builds, this can be overridden via webpack DefinePlugin.
actual val isDebug: Boolean = true
