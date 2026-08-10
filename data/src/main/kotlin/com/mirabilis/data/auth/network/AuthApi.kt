package com.mirabilis.data.auth.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Retrofit contract for the auth backend (contracts/auth-api.md). Auth endpoints carry the
 * [AuthInterceptor.NO_AUTH_HEADER] marker so no token is attached; `GET /me` is protected.
 */
interface AuthApi {

    @Headers("${AuthInterceptorHeaders.NO_AUTH}: true")
    @POST("otp/request")
    suspend fun requestOtp(@Body body: OtpRequestRequest): OtpRequestResponseRemote

    @Headers("${AuthInterceptorHeaders.NO_AUTH}: true")
    @POST("otp/verify")
    suspend fun verifyOtp(@Body body: VerifyRequest): VerifyResponseRemote

    @Headers("${AuthInterceptorHeaders.NO_AUTH}: true")
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponseRemote

    @GET("me")
    suspend fun me(): MeResponseRemote

    @PATCH("me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): MeResponseRemote
}

/** Header name shared by the API annotations and the interceptor (avoids a string literal drift). */
object AuthInterceptorHeaders {
    const val NO_AUTH = "No-Authentication"
}
