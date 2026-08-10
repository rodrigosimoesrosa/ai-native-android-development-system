package com.mirabilis.data.profile.network

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.network.AuthApi
import com.mirabilis.data.auth.network.AuthRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Contract test (contracts/profile-api.md): PATCH /me maps 200 → updated user, 400 → typed error. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileApiTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: AuthRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        dataSource = AuthRemoteDataSource(retrofit.create(AuthApi::class.java), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `updateProfile maps a 200 to Success with the updated user`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"user":{"id":"u_1","phone":"+15551234567","displayName":"Ada L."}}""",
            ),
        )

        val result = dataSource.updateProfile("Ada L.")

        assertTrue(result is Result.Success)
        assertEquals("Ada L.", (result as Result.Success).data.user.displayName)

        val recorded = server.takeRequest()
        assertEquals("PATCH", recorded.method)
        assertEquals("/me", recorded.path)
    }

    @Test
    fun `updateProfile maps a 400 invalid-name to a typed Server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"invalid_display_name"}"""))

        val result = dataSource.updateProfile("")

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is AppError.Server && error.status == 400)
    }
}
