package com.mirabilis.data.auth.network

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
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

/** Contract test (contracts/auth-api.md): drives AuthRemoteDataSource against MockWebServer and
 *  asserts the typed Result/AppError mapping for 2xx and error responses (US1: FR-001/003/004). */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthApiTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: AuthRemoteDataSource
    private var serverUp = true

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        serverUp = true
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        dataSource = AuthRemoteDataSource(retrofit.create(AuthApi::class.java), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        if (serverUp) server.shutdown()
    }

    @Test
    fun `requestOtp maps a 200 to Success with the verification token`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"verificationToken":"vt_abc"}"""))

        val result = dataSource.requestOtp("+15551234567")

        assertTrue(result is Result.Success)
        assertEquals("vt_abc", (result as Result.Success).data.verificationToken)
    }

    @Test
    fun `verifyOtp maps a 200 to Success with tokens and user`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"accessToken":"a","refreshToken":"r","expiresIn":900,
                   "user":{"id":"u_1","phone":"+15551234567","displayName":"Ada"}}""",
            ),
        )

        val result = dataSource.verifyOtp("vt_abc", "123456")

        assertTrue(result is Result.Success)
        assertEquals("a", (result as Result.Success).data.accessToken)
        assertEquals("u_1", result.data.user.id)
    }

    @Test
    fun `verifyOtp maps a 400 wrong-code to a typed Server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"invalid_code"}"""))

        val result = dataSource.verifyOtp("vt_abc", "000000")

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is AppError.Server && error.status == 400)
    }

    @Test
    fun `verifyOtp maps a 410 expired-code to a typed Server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(410).setBody("""{"error":"code_expired"}"""))

        val result = dataSource.verifyOtp("vt_abc", "123456")

        val error = (result as Result.Error).error
        assertTrue(error is AppError.Server && error.status == 410)
    }

    @Test
    fun `a connection failure maps to AppError_Network`() = runTest {
        server.shutdown()
        serverUp = false

        val result = dataSource.me()

        assertTrue(result is Result.Error && (result).error is AppError.Network)
    }
}
