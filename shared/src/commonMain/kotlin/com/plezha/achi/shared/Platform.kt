package com.plezha.achi.shared

/**
 * Whether the app is running in a debug build.
 * Used to gate debug-only UI (debug panel, quick login, etc.).
 */
expect val isDebug: Boolean
