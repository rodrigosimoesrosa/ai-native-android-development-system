package com.mirabilis.feature.auth.sendphone

import app.cash.turbine.test
import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.usecase.RequestOtpUseCase
import com.mirabilis.feature.auth.FakeAuthRepository
import com.mirabilis.feature.auth.PendingVerificationStore
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendPhoneViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeAuthRepository, store: PendingVerificationStore) =
        SendPhoneViewModel(RequestOtpUseCase(repo), store)

    @Test
    fun `invalid phone shows an error and never calls the backend`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val store = PendingVerificationStore()
        val vm = viewModel(repo, store)

        vm.setIntent { SendPhoneIntent.PhoneChanged("12345") }
        vm.setIntent { SendPhoneIntent.Submit }
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertEquals(0, repo.requestCallCount)
        assertNull(store.challenge)
    }

    @Test
    fun `valid phone stores the challenge and navigates to verify`() = runTest(dispatcher) {
        val repo = FakeAuthRepository()
        val store = PendingVerificationStore()
        val vm = viewModel(repo, store)

        vm.effects.test {
            vm.setIntent { SendPhoneIntent.PhoneChanged("+15551234567") }
            vm.setIntent { SendPhoneIntent.Submit }
            assertEquals(SendPhoneEffect.NavigateToVerify, awaitItem())
        }
        assertEquals(1, repo.requestCallCount)
        assertNotNull(store.challenge)
    }

    @Test
    fun `backend error surfaces a user message`() = runTest(dispatcher) {
        val repo = FakeAuthRepository(requestResult = Result.Error(AppError.Network))
        val store = PendingVerificationStore()
        val vm = viewModel(repo, store)

        vm.setIntent { SendPhoneIntent.PhoneChanged("+15551234567") }
        vm.setIntent { SendPhoneIntent.Submit }
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(store.challenge)
    }
}
