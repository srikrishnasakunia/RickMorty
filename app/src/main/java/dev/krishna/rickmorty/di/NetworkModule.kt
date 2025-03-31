package dev.krishna.rickmorty.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.krishna.rickmorty.utils.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkMonitor(context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
}
