package com.mirabilis.data.auth.di

import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.data.auth.datastore.SessionLocalDataSource
import com.mirabilis.data.auth.network.AuthApi
import com.mirabilis.data.auth.network.FakeAuthApi
import com.mirabilis.data.auth.network.AuthRemoteDataSource
import com.mirabilis.data.auth.network.IAuthRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Interface→impl bindings for `:data` (ADR-0004). Datasource impls have `@Inject` constructors;
 * these `@Binds` expose them behind their domain-facing interfaces. `AuthApi` is bound to the
 * [FakeAuthApi] (ADR-0006 §6) until a real backend is swapped in.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourcesModule {

    @Binds
    @Singleton
    abstract fun bindAuthApi(impl: FakeAuthApi): AuthApi

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSource): IAuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindSessionLocalDataSource(impl: SessionLocalDataSource): ISessionLocalDataSource
}
