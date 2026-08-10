package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.FakeSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignOutUseCaseTest {

    @Test
    fun `sign out clears the session and flips auth-state to false`() = runTest {
        val repo = FakeSessionRepository(initialAuthenticated = true)

        val result = SignOutUseCase(repo).invoke()

        assertTrue(result is Result.Success)
        assertEquals(1, repo.signOutCallCount)
        assertFalse(repo.observeAuthState().first())
    }
}
