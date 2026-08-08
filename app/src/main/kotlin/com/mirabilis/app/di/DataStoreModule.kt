package com.mirabilis.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.mirabilis.data.auth.datastore.SessionProto
import com.mirabilis.data.auth.datastore.SessionSerializer
import com.mirabilis.data.auth.datastore.UserProto
import com.mirabilis.data.auth.datastore.UserSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Creates the encrypted Proto DataStore instances (need `Context`, so they live at the composition
 * root — ADR-0004/0005). Serializers encrypt via Keystore (ADR-0006 §5).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
        serializer: SessionSerializer,
    ): DataStore<SessionProto> =
        DataStoreFactory.create(serializer = serializer) { context.dataStoreFile("session.pb") }

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
        serializer: UserSerializer,
    ): DataStore<UserProto> =
        DataStoreFactory.create(serializer = serializer) { context.dataStoreFile("user.pb") }
}
