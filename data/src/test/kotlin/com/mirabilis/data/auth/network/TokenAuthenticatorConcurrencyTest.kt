package com.mirabilis.data.auth.network

import com.mirabilis.data.auth.FakeSessionLocalDataSource
import com.mirabilis.data.auth.network.interceptor.AuthInterceptor
import com.mirabilis.data.auth.network.interceptor.TokenAuthenticator
import com.mirabilis.data.auth.session.SessionHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.atomic.AtomicInteger

/** US2 (FR-009, SC-004): concurrent 401s must trigger EXACTLY ONE refresh (single-flight). */
class TokenAuthenticatorConcurrencyTest {

    private lateinit var server: MockWebServer
    private val json = Json { ignoreUnknownKeys = true }
    private val refreshCount = AtomicInteger(0)

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        server.dispatcher = meRefreshDispatcher(refreshCount)
    }

    @After fun tearDown() = server.shutdown()

    @Test
    fun `concurrent 401s refresh only once and all succeed`() = runBlocking {
        val holder = SessionHolder().apply { update("OLD", "refresh_1") }
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
                    sessionLocal = FakeSessionLocalDataSource(holder),
                ),
            )
            .build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(AuthApi::class.java)

        val results = (1..8).map { async(Dispatchers.IO) { api.me() } }.awaitAll()

        assertEquals("exactly one refresh under concurrent 401s", 1, refreshCount.get())
        assertTrue("all protected calls succeeded", results.all { it.user.id == "u_1" })
        assertEquals("NEW", holder.accessToken)
    }
}
