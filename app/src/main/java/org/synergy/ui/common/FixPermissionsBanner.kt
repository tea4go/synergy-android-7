package org.synergy.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.synergy.R
import org.synergy.ui.theme.BarrierClientTheme

@Composable
fun FixPermissionsBanner(
    modifier: Modifier = Modifier,
    text: @Composable RowScope.() -> Unit = {},
    onLearnMoreClick: () -> Unit = {},
    onFixClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
        ) { text(this) }
        Spacer(modifier = Modifier.requiredHeight(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onLearnMoreClick) {
                Text(
                    text = stringResource(id = R.string.learn_more).uppercase(),
                    style = MaterialTheme.typography.button,
                )
            }
            TextButton(onClick = onFixClick) {
                Text(
                    text = stringResource(id = R.string.fix_it).uppercase(),
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewFixPermissionsBanner() {
    BarrierClientTheme {
        Surface {
            FixPermissionsBanner(
                text = {
                    Text(text = "Lorem ipsum dolor sit amet, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                }
            )
        }
    }
}