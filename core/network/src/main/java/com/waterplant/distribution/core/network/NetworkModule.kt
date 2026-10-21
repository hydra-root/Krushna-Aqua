package com.waterplant.distribution.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSyncHttpClient(): SyncHttpClient {
        return SyncHttpClient()
    }

    @Provides
    @Singleton
    fun provideSyncHttpServer(): SyncHttpServer {
        return SyncHttpServer()
    }
}
