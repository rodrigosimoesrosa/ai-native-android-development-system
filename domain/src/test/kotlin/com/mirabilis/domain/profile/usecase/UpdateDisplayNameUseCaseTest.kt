package com.mirabilis.domain.profile.usecase

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.profile.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** US2/FR-002: a blank display name is rejected before any persistence; a valid one is delegated. */
class UpdateDisplayNameUseCaseTest {

    @Test
    fun `blank name is rejected and nothing is persisted`() = runTest {
        val repo = FakeProfileRepository()
        val useCase = UpdateDisplayNameUseCase(repo)

        val result = useCase("   ")

        assertTrue(result is Result.Error && (result).error is AppError.Validation)
        assertEquals(0, repo.callCount)
    }

    @Test
    fun `valid name is trimmed and delegated to the repository`() = runTest {
        val repo = FakeProfileRepository()
        val useCase = UpdateDisplayNameUseCase(repo)

        val result = useCase("  Ada L.  ")

        assertTrue(result is Result.Success)
        assertEquals(1, repo.callCount)
        assertEquals("Ada L.", repo.lastRequestedName)
    }
}
