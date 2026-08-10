package com.mirabilis.data.profile.di

import com.mirabilis.data.profile.preferences.IPreferencesLocalDataSource
import com.mirabilis.data.profile.preferences.PreferencesLocalDataSource
import com.mirabilis.data.profile.repository.PreferencesRepository
import com.mirabilis.data.profile.repository.ProfileRepository
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import com.mirabilis.domain.profile.repository.IProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Interface→impl bindings for the profile feature (ADR-0004: bindings live with the impl in `:data`). */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepository): IProfileRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepository): IPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesLocalDataSource(
        impl: PreferencesLocalDataSource,
    ): IPreferencesLocalDataSource
}
