package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestOtpUseCaseTest {

    @Test
    fun `rejects malformed phone before calling the backend`() = runTest {
        val repo = FakeAuthRepository()
        val result = RequestOtpUseCase(repo).invoke("12345")

        assertTrue(result is Result.Error && result.error is AppError.Validation)
        assertEquals(0, repo.requestCallCount)
        assertNull(repo.requestedPhone)
    }

    @Test
    fun `trims and delegates a valid E164 phone`() = runTest {
        val repo = FakeAuthRepository()
        val result = RequestOtpUseCase(repo).invoke("  +15551234567 ")

        assertTrue(result is Result.Success)
        assertEquals("+15551234567", repo.requestedPhone)
        assertEquals(1, repo.requestCallCount)
    }
}
