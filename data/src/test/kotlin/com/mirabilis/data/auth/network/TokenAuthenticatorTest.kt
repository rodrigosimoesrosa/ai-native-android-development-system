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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.atomic.AtomicInteger

/** US2 (FR-008, SC-003): a 401 on a protected call triggers a transparent refresh + retry. */
class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private val json = Json { ignoreUnknownKeys = true }
    private val refreshCount = AtomicInteger(0)

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        server.dispatcher = meRefreshDispatcher(refreshCount)
    }

    @After fun tearDown() = server.shutdown()

    private fun authedApi(holder: SessionHolder): AuthApi {
        val refreshApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(AuthApi::class.java)
        val authenticator = TokenAuthenticator(
            sessionHolder = holder,
            tokenRefresher = RetrofitTokenRefresher(refreshApi, Dispatchers.IO),
            sessionLocal = FakeSessionLocalDataSource(holder),
        )
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(holder))
            .authenticator(authenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(AuthApi::class.java)
    }

    @Test
    fun `401 triggers refresh then retries with the new token`() = runBlocking {
        val holder = SessionHolder().apply { update("OLD", "refresh_1") }
        val api = authedApi(holder)

        val response = api.me()

        assertEquals("u_1", response.user.id)
        assertEquals(1, refreshCount.get())
        assertEquals("NEW", holder.accessToken)
    }
}

/** Shared MockWebServer behavior: /me is 401 unless the token is NEW; /auth/refresh mints NEW. */
internal fun meRefreshDispatcher(refreshCount: AtomicInteger): Dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path.orEmpty()
        return when {
            "auth/refresh" in path -> {
                refreshCount.incrementAndGet()
                MockResponse().setResponseCode(200)
                    .setBody("""{"accessToken":"NEW","refreshToken":"refresh_2","expiresIn":900}""")
            }
            "me" in path -> {
                if (request.getHeader("Authorization") == "Bearer NEW") {
                    MockResponse().setResponseCode(200)
                        .setBody("""{"user":{"id":"u_1","phone":"+15551234567","displayName":"Ada"}}""")
                } else {
                    MockResponse().setResponseCode(401).setBody("""{"error":"unauthorized"}""")
                }
            }
            else -> MockResponse().setResponseCode(404)
        }
    }
}
