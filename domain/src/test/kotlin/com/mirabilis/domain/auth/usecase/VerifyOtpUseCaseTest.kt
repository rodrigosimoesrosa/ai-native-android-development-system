package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.FakeAuthRepository
import com.mirabilis.domain.auth.model.AuthSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifyOtpUseCaseTest {

    @Test
    fun `returns the session on a correct code`() = runTest {
        val session = AuthSession("access", "refresh", 900)
        val repo = FakeAuthRepository(verifyResult = Result.Success(session))

        val result = VerifyOtpUseCase(repo).invoke("vt_1", "123456")

        assertEquals(Result.Success(session), result)
        assertEquals("vt_1" to "123456", repo.verifyArgs)
    }

    @Test
    fun `propagates a server error on a wrong code`() = runTest {
        val repo = FakeAuthRepository(verifyResult = Result.Error(AppError.Server(400, "invalid_code")))

        val result = VerifyOtpUseCase(repo).invoke("vt_1", "000000")

        assertTrue(result is Result.Error && result.error is AppError.Server)
    }
}
