package com.mirabilis.data.auth.di

import com.mirabilis.data.auth.repository.AuthRepository
import com.mirabilis.data.auth.repository.SessionRepository
import com.mirabilis.domain.auth.repository.IAuthRepository
import com.mirabilis.domain.auth.repository.ISessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository interface→impl bindings (ADR-0004: bindings live where the impl lives, i.e. `:data`).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepository): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepository): ISessionRepository
}
