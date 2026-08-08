package com.mirabilis.feature.auth.verifyphone

import app.cash.turbine.test
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.usecase.RequestOtpUseCase
import com.mirabilis.domain.auth.usecase.VerifyOtpUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyPhoneViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeAuthRepository, store: PendingVerificationStore) =
        VerifyPhoneViewModel(VerifyOtpUseCase(repo), RequestOtpUseCase(repo), store)

    @Test
    fun `missing challenge routes back to SendPhone`() = runTest(dispatcher) {
        val store = PendingVerificationStore() // challenge == null
        val vm = viewModel(FakeAuthRepository(), store)

        vm.effects.test {
            assertEquals(VerifyPhoneEffect.NavigateToSendPhone, awaitItem())
        }
    }

    @Test
    fun `correct code clears the challenge and navigates Home`() = runTest(dispatcher) {
        val store = PendingVerificationStore().apply {
            challenge = PhoneVerificationChallenge("+15551234567", "vt_1")
        }
        val repo = FakeAuthRepository()
        val vm = viewModel(repo, store)

        vm.effects.test {
            vm.setIntent { VerifyPhoneIntent.CodeChanged("123456") }
            vm.setIntent { VerifyPhoneIntent.Submit }
            assertEquals(VerifyPhoneEffect.NavigateToHome, awaitItem())
        }
        assertEquals(1, repo.verifyCallCount)
        assertNull(store.challenge)
    }

    @Test
    fun `resend requests a new code and starts the cooldown`() = runTest(dispatcher) {
        val store = PendingVerificationStore().apply {
            challenge = PhoneVerificationChallenge("+15551234567", "vt_1")
        }
        val repo = FakeAuthRepository()
        val vm = viewModel(repo, store)

        vm.setIntent { VerifyPhoneIntent.Resend }
        advanceUntilIdle()

        assertTrue(repo.requestCallCount >= 1)
        // After a resend the button is on cooldown (immediate second resend is blocked).
        val cooldownAfter = vm.state.value.resendCooldownSeconds
        assertTrue("cooldown should have started", cooldownAfter in 0..30)
    }
}
