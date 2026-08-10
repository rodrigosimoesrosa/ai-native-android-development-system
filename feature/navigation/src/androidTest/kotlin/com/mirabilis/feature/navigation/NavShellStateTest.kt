package com.mirabilis.feature.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T007 [US2]: instrumented Compose-nav behavior over [NavShellScaffold] with placeholder destinations
 * (US1 independent-test note). Proves switching (scenario 1), no disruptive reload on re-select
 * (scenario 2), and nested per-destination state kept across a switch (scenario 3 / FR-003).
 */
@RunWith(AndroidJUnit4::class)
class NavShellStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val tabA = Destination(route = "a", label = "Alpha", icon = "A")
    private val tabB = Destination(route = "b", label = "Beta", icon = "B")

    private fun setShell() {
        composeTestRule.setContent {
            NavShellScaffold(
                destinations = listOf(tabA, tabB),
                startDestination = "a",
            ) {
                composable("a") {
                    var count by rememberSaveable { mutableIntStateOf(0) }
                    Column {
                        Text(text = "A count: $count", modifier = Modifier.testTag("a_count"))
                        Button(
                            modifier = Modifier.testTag("a_inc"),
                            onClick = { count++ },
                        ) { Text("inc") }
                    }
                }
                composable("b") {
                    Text(text = "Beta screen", modifier = Modifier.testTag("b_screen"))
                }
            }
        }
    }

    @Test
    fun switchesBetweenDestinations() {
        setShell()
        composeTestRule.onNodeWithTag("a_count").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_b").performClick()
        composeTestRule.onNodeWithTag("b_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_a").performClick()
        composeTestRule.onNodeWithTag("a_count").assertIsDisplayed()
    }

    @Test
    fun reSelectingCurrentTabDoesNotResetItsState() {
        setShell()
        composeTestRule.onNodeWithTag("a_inc").performClick()
        composeTestRule.onNodeWithText("A count: 1").assertIsDisplayed()

        // Re-select the already-active tab (launchSingleTop) — must not reload/reset.
        composeTestRule.onNodeWithTag("tab_a").performClick()
        composeTestRule.onNodeWithText("A count: 1").assertIsDisplayed()
    }

    @Test
    fun keepsNestedStateAcrossSwitch() {
        setShell()
        composeTestRule.onNodeWithTag("a_inc").performClick()
        composeTestRule.onNodeWithTag("a_inc").performClick()
        composeTestRule.onNodeWithText("A count: 2").assertIsDisplayed()

        // Switch away…
        composeTestRule.onNodeWithTag("tab_b").performClick()
        composeTestRule.onNodeWithTag("b_screen").assertIsDisplayed()

        // …and back: A's state is preserved (saveState / restoreState).
        composeTestRule.onNodeWithTag("tab_a").performClick()
        composeTestRule.onNodeWithText("A count: 2").assertIsDisplayed()
    }
}
