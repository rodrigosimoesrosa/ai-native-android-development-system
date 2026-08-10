package com.mirabilis.data.auth.network

import com.mirabilis.data.auth.FakeSessionLocalDataSource
import com.mirabilis.data.auth.network.interceptor.AuthInterceptor
import com.mirabilis.data.auth.network.interceptor.TokenAuthenticator
import com.mirabilis.data.auth.session.SessionHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** US3 (FR-010): when refresh itself fails, the authenticator gives up and clears the session. */
class TokenAuthenticatorLogoutTest {

    private lateinit var server: MockWebServer
    private val json = Json { ignoreUnknownKeys = true }

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        // /me always 401, /auth/refresh also 401 → refresh is non-renewable.
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse().setResponseCode(401).setBody("""{"error":"unauthorized"}""")
        }
    }

    @After fun tearDown() = server.shutdown()

    @Test
    fun `non-renewable refresh clears the session and gives up`() = runBlocking {
        val holder = SessionHolder().apply { update("OLD", "refresh_1") }
        val fakeLocal = FakeSessionLocalDataSource(holder)
        val refreshApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(AuthApi::class.java)
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(holder))
            .authenticator(
                TokenAuthenticator(
                    sessionHolder = holder,
                    tokenRefresher = RetrofitTokenRefresher(refreshApi, Dispatchers.IO),
                    sessionLocal = fakeLocal,
                ),
            )
            .build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(AuthApi::class.java)

        var threw = false
        try {
            api.me()
        } catch (e: HttpException) {
            threw = true
        }

        assertTrue("gives up with the 401", threw)
        assertNull("session cleared on non-renewable refresh", holder.accessToken)
        assertNull(holder.refreshToken)
    }
}
