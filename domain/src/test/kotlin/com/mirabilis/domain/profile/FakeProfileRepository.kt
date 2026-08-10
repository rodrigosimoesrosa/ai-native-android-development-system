package com.mirabilis.domain.profile

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.profile.repository.IProfileRepository

/** Test double for [IProfileRepository]; records the last requested name and returns a canned result. */
class FakeProfileRepository(
    private val result: Result<User> = Result.Success(User("u_1", "+15551234567", "Ada L.")),
) : IProfileRepository {

    var lastRequestedName: String? = null
    var callCount: Int = 0

    override suspend fun updateDisplayName(displayName: String): Result<User> {
        callCount++
        lastRequestedName = displayName
        return result
    }
}
