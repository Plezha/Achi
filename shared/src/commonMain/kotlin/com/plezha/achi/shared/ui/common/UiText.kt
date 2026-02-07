package com.plezha.achi.shared.ui.common

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Abstraction for text that can be either a raw string or a localizable string resource.
 * ViewModels emit UiText, and the UI layer resolves it via [asString] (composable) or [resolve] (suspend).
 */
sealed class UiText {
    data class Raw(val value: String) : UiText()
    class Resource(val res: StringResource, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is Raw -> value
        is Resource -> if (args.isEmpty()) stringResource(res) else stringResource(res, *args)
    }

    /**
     * Suspend variant for use in coroutine scopes (e.g. LaunchedEffect / collectLatest).
     */
    suspend fun resolve(): String = when (this) {
        is Raw -> value
        is Resource -> if (args.isEmpty()) getString(res) else getString(res, *args)
    }
}
