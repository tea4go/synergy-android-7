package org.synergy.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import org.synergy.R
import org.synergy.utils.LocalToolbarState

@Composable
fun SettingsScreen(
    title: String = stringResource(id = R.string.settings),
) {
    val toolbarState = LocalToolbarState.current

    LaunchedEffect(Unit) {
        toolbarState.setTitle(title)
    }

    SettingsScreenContent(

    )
}