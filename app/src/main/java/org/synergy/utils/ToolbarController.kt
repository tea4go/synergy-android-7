package org.synergy.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import org.synergy.R

class ToolbarState {
    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private var _actions: MutableState<(@Composable RowScope.() -> Unit)?> = mutableStateOf(null)
    val actions: State<(@Composable RowScope.() -> Unit)?> = _actions

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setActions(actions: (@Composable RowScope.() -> Unit)?) {
        _actions.value = actions
    }
}

val LocalToolbarState = compositionLocalOf { ToolbarState() }

@Composable
fun ProvideToolbarState(
    title: String = stringResource(id = R.string.app_name),
    content: @Composable () -> Unit,
) {
    val toolbarState = remember {
        ToolbarState().apply {
            setTitle(title)
        }
    }

    CompositionLocalProvider(LocalToolbarState provides toolbarState) {
        content()
    }
}