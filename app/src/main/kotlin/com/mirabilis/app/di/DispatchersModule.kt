package com.mirabilis.app.di

import com.mirabilis.core.dispatcher.Dispatcher
import com.mirabilis.core.dispatcher.DispatcherType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Provides qualified dispatchers (ADR-0004). The qualifier lives in pure `:core`. */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(DispatcherType.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(DispatcherType.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
