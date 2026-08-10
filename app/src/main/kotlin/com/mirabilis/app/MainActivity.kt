package com.mirabilis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.feature.auth.navigation.AuthRoot
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host. [AuthRoot] picks the start destination from the persisted auth state; the
 *  persisted theme preference (US3/FR-004) drives [MaterialTheme]. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme by themeViewModel.theme.collectAsState()
            val darkTheme = when (theme) {
                Theme.System -> isSystemInDarkTheme()
                Theme.Light -> false
                Theme.Dark -> true
            }
            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                Surface {
                    AuthRoot()
                }
            }
        }
    }
}
