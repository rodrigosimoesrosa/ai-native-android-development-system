package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingMd

/**
 * A branded top app bar component providing a title slot plus optional navigation icon
 * and action slots.
 *
 * Consumes design-system tokens exclusively — no raw hex/dp literals for appearance.
 * The navigation icon uses `onSurface`/`surfaceVariant` tokens; the title uses
 * `titleMedium` typography; actions use the same icon styling as the navigation icon.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param title The title to display in the center of the bar.
 * @param modifier Modifier for styling or layout positioning.
 * @param navigationIcon Optional leading icon (e.g. back arrow). When non-null, a clickable
 *                       icon slot is rendered using `onSurface` tokens.
 * @param actions Composable lambda for action icons rendered on the trailing side.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MirabilisTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.testTag("MirabilisTopAppBar:Title"),
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.testTag("MirabilisTopAppBar:NavigationIcon"),
                ) {
                    navigationIcon()
                }
            }
        },
        actions = { actions() },
        modifier = modifier
            .testTag("MirabilisTopAppBar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTopAppBarLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingMd),
        ) {
            Text("With Navigation Icon", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                },
            )
            Text("With Actions", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                actions = {
                    Text("Action", style = MaterialTheme.typography.bodyMedium)
                },
            )
            Text("With Navigation + Actions", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                },
                actions = {
                    Text("More", style = MaterialTheme.typography.bodyMedium)
                },
            )
            Text("Minimal (no icon, no actions)", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(title = "Minimal Title")
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTopAppBarDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingMd),
        ) {
            Text("With Navigation Icon", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                },
            )
            Text("With Actions", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                actions = {
                    Text("Action", style = MaterialTheme.typography.bodyMedium)
                },
            )
            Text("With Navigation + Actions", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(
                title = "Screen Title",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                },
                actions = {
                    Text("More", style = MaterialTheme.typography.bodyMedium)
                },
            )
            Text("Minimal (no icon, no actions)", style = MaterialTheme.typography.titleMedium)
            MirabilisTopAppBar(title = "Minimal Title")
        }
    }
}
