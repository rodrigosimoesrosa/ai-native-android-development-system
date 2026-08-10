package com.mirabilis.data.profile.repository

import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.data.auth.mapper.toDomain
import com.mirabilis.data.auth.network.IAuthRemoteDataSource
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.profile.repository.IProfileRepository
import javax.inject.Inject

/**
 * Updates the display name via the mock `PATCH /me` and persists the returned user locally
 * (contracts/profile-api). On success Home/Profile show the new name on the next `observeUser`.
 */
class ProfileRepository @Inject constructor(
    private val remote: IAuthRemoteDataSource,
    private val local: ISessionLocalDataSource,
) : IProfileRepository {

    override suspend fun updateDisplayName(displayName: String): Result<User> =
        when (val response = remote.updateProfile(displayName)) {
            is Result.Success -> {
                val user = response.data.user.toDomain()
                when (val saved = local.updateUser(user)) {
                    is Result.Success -> Result.Success(user)
                    is Result.Error -> saved
                }
            }
            is Result.Error -> response
        }
}
