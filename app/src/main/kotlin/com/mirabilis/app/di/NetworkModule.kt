package com.mirabilis.app.di

import com.mirabilis.data.auth.network.AuthApi
import com.mirabilis.data.auth.network.RefreshClient
import com.mirabilis.data.auth.network.interceptor.AuthInterceptor
import com.mirabilis.data.auth.network.interceptor.TokenAuthenticator
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
 * Networking infrastructure (ADR-0006). Provides the **authed** OkHttp client ([AuthInterceptor]
 * attaches the bearer; [TokenAuthenticator] handles 401→refresh→retry) + Retrofit + JSON, and a
 * separate **refresh-only** client/Retrofit/[AuthApi] (`@RefreshClient`) with neither, so a 401 on
 * `/auth/refresh` cannot recurse. `AuthApi` (unqualified) is bound to the fake in `:data` (§6).
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

    // --- Authed client (bearer + transparent refresh) ---

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    // --- Refresh-only client (anti-loop: no interceptor, no authenticator) ---

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshRetrofit(@RefreshClient client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshAuthApi(@RefreshClient retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    private const val BASE_URL = "https://mock.mirabilis.local/"
}
