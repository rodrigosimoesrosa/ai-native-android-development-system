package com.mirabilis.feature.auth.home

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.usecase.GetCurrentUserUseCase
import com.mirabilis.feature.auth.FakeAuthRepository
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

    @Test
    fun `loads the authenticated user on init`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(currentUserResult = Result.Success(User("u_1", "+15551234567", "Ada")))
        val vm = HomeViewModel(GetCurrentUserUseCase(repo))
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Ada", vm.state.value.user?.displayName)
    }

    @Test
    fun `shows an error when the profile call fails`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(currentUserResult = Result.Error(AppError.Network))
        val vm = HomeViewModel(GetCurrentUserUseCase(repo))
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNotNull(vm.state.value.error)
    }
}
