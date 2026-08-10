package com.mirabilis.data.auth.repository

import com.mirabilis.data.auth.FakeSessionLocalDataSource
import com.mirabilis.data.auth.session.SessionHolder
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** US2 (FR-006, SC-002): a stored valid session ⇒ auth-state is true (restored on restart). */
class SessionRepositoryTest {

    @Test
    fun `auth-state is true when a valid session is stored`() = runTest {
        val local = FakeSessionLocalDataSource()
        local.save(AuthSession("access", "refresh", 900), User("u_1", "+15551234567", "Ada"))
        val repo = SessionRepository(local)

        assertTrue(repo.observeAuthState().first())
    }

    @Test
    fun `auth-state is false when no session is stored`() = runTest {
        val repo = SessionRepository(FakeSessionLocalDataSource())

        assertFalse(repo.observeAuthState().first())
    }

    @Test
    fun `clearSession makes auth-state false`() = runTest {
        val holder = SessionHolder()
        val local = FakeSessionLocalDataSource(holder)
        local.save(AuthSession("access", "refresh", 900), User("u_1", "+15551234567", "Ada"))
        val repo = SessionRepository(local)

        repo.clearSession()

        assertFalse(repo.observeAuthState().first())
        assertFalse(holder.accessToken != null)
    }
}
