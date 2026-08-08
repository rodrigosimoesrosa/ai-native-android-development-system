package com.mirabilis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.mirabilis.feature.auth.navigation.AuthNavGraph
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host. Top-level auth-state routing is wired in via [AuthNavGraph]. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    AuthNavGraph(navController = navController)
                }
            }
        }
    }
}
