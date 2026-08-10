package com.mirabilis.feature.auth.home

import app.cash.turbine.test
import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.usecase.GetCurrentUserUseCase
import com.mirabilis.domain.auth.usecase.ObserveAuthStateUseCase
import com.mirabilis.domain.auth.usecase.SignOutUseCase
import com.mirabilis.feature.auth.FakeAuthRepository
import com.mirabilis.feature.auth.FakeSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        auth: FakeAuthRepository,
        session: FakeSessionRepository,
    ) = HomeViewModel(
        getCurrentUser = GetCurrentUserUseCase(auth),
        signOutUseCase = SignOutUseCase(session),
        observeAuthState = ObserveAuthStateUseCase(session),
    )

    @Test
    fun `loads the authenticated user on init`() = runTest(dispatcher) {
        val auth = FakeAuthRepository(currentUserResult = Result.Success(User("u_1", "+15551234567", "Ada")))
        val vm = viewModel(auth, FakeSessionRepository(initialAuthenticated = true))
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Ada", vm.state.value.user?.displayName)
    }

    @Test
    fun `shows an error when the profile call fails`() = runTest(dispatcher) {
        val auth = FakeAuthRepository(currentUserResult = Result.Error(AppError.Network))
        val vm = viewModel(auth, FakeSessionRepository(initialAuthenticated = true))
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `sign out routes to SendPhone`() = runTest(dispatcher) {
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(FakeAuthRepository(), session)

        vm.effects.test {
            vm.setIntent { HomeIntent.SignOut }
            assertEquals(HomeEffect.NavigateToSendPhone, awaitItem())
        }
        assertEquals(1, session.signOutCallCount)
    }

    @Test
    fun `a non-renewable session (auth-state false) routes to SendPhone`() = runTest(dispatcher) {
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(FakeAuthRepository(), session)

        vm.effects.test {
            session.authState.value = false // simulates the authenticator clearing the session
            assertEquals(HomeEffect.NavigateToSendPhone, awaitItem())
        }
    }
}
