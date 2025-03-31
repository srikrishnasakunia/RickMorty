package dev.krishna.rickmorty.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.krishna.rickmorty.data.api.RickMortyApiService
import dev.krishna.rickmorty.data.database.AppDatabase
import dev.krishna.rickmorty.data.repository.CharacterRepository
import dev.krishna.rickmorty.utils.NetworkMonitor

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun providesCharacterRepository(
        apiService: RickMortyApiService,
        appDatabase: AppDatabase
    ): CharacterRepository{
        return CharacterRepository(apiService, appDatabase)
    }
}