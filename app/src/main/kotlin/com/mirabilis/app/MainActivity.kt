package com.mirabilis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.mirabilis.feature.auth.navigation.AuthRoot
import com.mirabilis.feature.navigation.NavShell
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. [AuthRoot] picks the start destination from the persisted auth state and, when
 * authenticated, hosts the navigation shell ([NavShell]) — wired here so `:feature:auth` does not depend
 * on `:feature:navigation` (the dependency runs `navigation → auth`).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AuthRoot(
                        authenticatedContent = { onSignedOut -> NavShell(onSignedOut = onSignedOut) },
                    )
                }
            }
        }
    }
}
