package org.synergy.utils

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import org.synergy.R

class ToolbarState {
    private val _title = mutableStateOf("")
    val title: State<String> = _title

    fun setTitle(title: String) {
        _title.value = title
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