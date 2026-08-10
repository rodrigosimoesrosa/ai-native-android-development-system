package com.mirabilis.feature.auth

import com.mirabilis.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR-004/FR-014/SC-006: every AppError maps to a clear, non-sensitive user message. */
class ErrorMessagesTest {

    @Test
    fun `validation surfaces its own message`() {
        assertEquals("Bad phone", AppError.Validation("Bad phone").toUserMessage())
    }

    @Test
    fun `rate-limit (429) is reflected as a wait message (FR-014)`() {
        val message = AppError.Server(429, """{"error":"too_many_attempts"}""").toUserMessage()
        assertTrue(message.contains("Too many attempts"))
        // Non-sensitive: never leak the raw status or server body.
        assertFalse(message.contains("429"))
        assertFalse(message.contains("too_many_attempts"))
    }

    @Test
    fun `wrong and expired codes get distinct, friendly messages`() {
        assertTrue(AppError.Server(400, null).toUserMessage().contains("valid", ignoreCase = true))
        assertTrue(AppError.Server(410, null).toUserMessage().contains("expired", ignoreCase = true))
    }

    @Test
    fun `network and unknown errors never crash and stay generic`() {
        assertTrue(AppError.Network.toUserMessage().isNotBlank())
        assertTrue(AppError.Unknown(RuntimeException("boom")).toUserMessage().isNotBlank())
        // The raw throwable message must not leak.
        assertFalse(AppError.Unknown(RuntimeException("boom")).toUserMessage().contains("boom"))
    }
}
