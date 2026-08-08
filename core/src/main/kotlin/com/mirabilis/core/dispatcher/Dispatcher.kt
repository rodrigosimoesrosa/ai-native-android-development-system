package com.mirabilis.core.dispatcher

import javax.inject.Qualifier

/**
 * DI qualifier for coroutine dispatchers, declared in the pure `:core` module (ADR-0004 allows
 * `javax.inject` here). The providing `@Module` lives in an Android module (`:app`). Lets
 * `Safe*DataSource` receive the IO dispatcher by qualifier without coupling `:core` to Hilt.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val type: DispatcherType)

enum class DispatcherType { IO, Default }
