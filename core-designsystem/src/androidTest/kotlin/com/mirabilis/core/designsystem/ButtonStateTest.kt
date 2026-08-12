package com.mirabilis.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mirabilis.core.designsystem.component.MirabilisButton
import com.mirabilis.core.designsystem.component.MirabilisButtonStyle
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T020 [US1]: instrumented test verifying that a disabled button is not clickable.
 *
 * Tests all three button styles (Primary, Secondary, Text) in disabled state —
 * an edge case: `performClick()` on a disabled button must NOT trigger the
 * click callback (FR-008: disabled state).
 */
@RunWith(AndroidJUnit4::class)
class ButtonStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun disabledPrimaryButtonIsNotClickable() {
        composeTestRule.setContent {
            MirabilisTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    MirabilisButton(
                        text = "Disabled Primary",
                        onClick = { error("Click should NOT fire on disabled button") },
                        enabled = false,
                        modifier = Modifier.testTag("disabled_primary"),
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("disabled_primary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("disabled_primary").performClick()
    }

    @Test
    fun disabledSecondaryButtonIsNotClickable() {
        composeTestRule.setContent {
            MirabilisTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    MirabilisButton(
                        text = "Disabled Secondary",
                        onClick = { error("Click should NOT fire on disabled button") },
                        style = MirabilisButtonStyle.Secondary,
                        enabled = false,
                        modifier = Modifier.testTag("disabled_secondary"),
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("disabled_secondary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("disabled_secondary").performClick()
    }

    @Test
    fun disabledTextButtonIsNotClickable() {
        composeTestRule.setContent {
            MirabilisTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    MirabilisButton(
                        text = "Disabled Text",
                        onClick = { error("Click should NOT fire on disabled button") },
                        style = MirabilisButtonStyle.Text,
                        enabled = false,
                        modifier = Modifier.testTag("disabled_text"),
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("disabled_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("disabled_text").performClick()
    }
}
