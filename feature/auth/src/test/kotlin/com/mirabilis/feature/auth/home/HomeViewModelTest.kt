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
import org.junit.Assert.assertTrue
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
    fun `a non-renewable session (auth-state false) routes to SendPhone`() = runTest(dispatcher) {
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(FakeAuthRepository(), session)

        vm.effects.test {
            session.authState.value = false // simulates the authenticator clearing the session
            assertEquals(HomeEffect.NavigateToSendPhone, awaitItem())
        }
    }

    @Test
    fun `SignOutRequested sets showSignOutConfirm=true and does NOT invoke SignOutUseCase`() = runTest(dispatcher) {
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(FakeAuthRepository(), session)

        advanceUntilIdle()
        assertEquals(false, vm.state.value.showSignOutConfirm)
        vm.setIntent { HomeIntent.SignOutRequested }
        advanceUntilIdle()
        assertTrue(vm.state.value.showSignOutConfirm)
        assertEquals(0, session.signOutCallCount)
    }

    @Test
    fun `SignOutConfirmed invokes SignOutUseCase and emits NavigateToSendPhone on success`() = runTest(dispatcher) {
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(FakeAuthRepository(), session)

        advanceUntilIdle()
        vm.effects.test {
            vm.setIntent { HomeIntent.SignOutConfirmed }
            advanceUntilIdle()
            assertEquals(1, session.signOutCallCount)
            assertEquals(HomeEffect.NavigateToSendPhone, awaitItem())
        }
    }

    @Test
    fun `SignOutConfirmed with Error keeps user, sets signOutError, emits NO NavigateToSendPhone`() =
        runTest(dispatcher) {
            val auth = FakeAuthRepository(currentUserResult = Result.Success(User("u_1", "+15551234567", "Ada")))
            val session = FakeSessionRepository(
                initialAuthenticated = true,
                signOutResult = Result.Error(AppError.Network),
            )
            val vm = viewModel(auth, session)

            advanceUntilIdle()
            assertEquals("Ada", vm.state.value.user?.displayName)
            assertEquals(false, vm.state.value.showSignOutConfirm)
            assertEquals(null, vm.state.value.signOutError)

            vm.effects.test {
                vm.setIntent { HomeIntent.SignOutConfirmed }
                advanceUntilIdle()
                assertEquals(1, session.signOutCallCount)
                // User must still be present (not signed out)
                assertEquals("Ada", vm.state.value.user?.displayName)
                // Dialog should be closed
                assertEquals(false, vm.state.value.showSignOutConfirm)
                // Error message should be surfaced
                assertNotNull(vm.state.value.signOutError)
                // NO NavigateToSendPhone — user stays signed in
                expectNoEvents()
            }
        }

    @Test
    fun `SignOutCancelled clears dialog, does NOT invoke SignOutUseCase, user unchanged`() = runTest(dispatcher) {
        val auth = FakeAuthRepository(currentUserResult = Result.Success(User("u_1", "+15551234567", "Ada")))
        val session = FakeSessionRepository(initialAuthenticated = true)
        val vm = viewModel(auth, session)

        advanceUntilIdle()
        assertEquals("Ada", vm.state.value.user?.displayName)
        assertEquals(false, vm.state.value.showSignOutConfirm)

        // First: show the dialog via SignOutRequested
        vm.setIntent { HomeIntent.SignOutRequested }
        advanceUntilIdle()
        assertTrue(vm.state.value.showSignOutConfirm)
        assertEquals(0, session.signOutCallCount)

        // Then: cancel — clears the dialog, does NOT call signOutUseCase, user stays
        vm.setIntent { HomeIntent.SignOutCancelled }
        advanceUntilIdle()
        assertEquals(false, vm.state.value.showSignOutConfirm)
        assertEquals("Ada", vm.state.value.user?.displayName)
        assertEquals(0, session.signOutCallCount)
    }
}
