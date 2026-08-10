package com.mirabilis.feature.profile.profile

import app.cash.turbine.test
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.ISessionRepository
import com.mirabilis.domain.auth.usecase.ObserveUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** US1 (FR-001): Profile loads the authenticated user from the session. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val user = User(id = "u_1", phone = "+15551234567", displayName = "Ada Lovelace")

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeSession(user: User?) : ISessionRepository {
        val userFlow = MutableStateFlow(user)
        override fun observeAuthState(): Flow<Boolean> = MutableStateFlow(true)
        override fun observeUser(): Flow<User?> = userFlow
        override suspend fun signOut(): Result<Unit> = Result.Success(Unit)
        override suspend fun clearSession(): Result<Unit> = Result.Success(Unit)
    }

    private fun viewModel(session: ISessionRepository) =
        ProfileViewModel(ObserveUserUseCase(session))

    @Test
    fun `loads the authenticated user`() = runTest(dispatcher) {
        val vm = viewModel(FakeSession(user))

        vm.state.test {
            assertEquals(true, awaitItem().isLoading) // initial
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(user, loaded.user)
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `null user surfaces a recoverable error`() = runTest(dispatcher) {
        val vm = viewModel(FakeSession(null))

        vm.state.test {
            assertEquals(true, awaitItem().isLoading) // initial
            val failed = awaitItem()
            assertFalse(failed.isLoading)
            assertNull(failed.user)
            assertEquals("Couldn't load your profile.", failed.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
