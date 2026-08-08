package com.mirabilis.app.di

import com.mirabilis.data.auth.network.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Networking infrastructure (ADR-0006). Provides the authed OkHttp client (with [AuthInterceptor])
 * + Retrofit + JSON. `AuthApi` itself is bound to the fake in `:data` (ADR-0006 §6).
 *
 * NOTE (Phase 4 / US2): the refresh-only client and the `TokenAuthenticator` attach here later
 * (tasks T047/T049). Kept out now so the foundational build has no 401-refresh surface yet.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private const val BASE_URL = "https://mock.mirabilis.local/"
}
