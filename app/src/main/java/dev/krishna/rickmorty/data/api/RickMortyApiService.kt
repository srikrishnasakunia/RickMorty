package dev.krishna.rickmorty.data.api

import dev.krishna.rickmorty.data.api.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RickMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page")
        page: Int = 1,
        @Query("name")
        name: String? = null,
        @Query("status")
        status: String? = null,
        @Query("species")
        species: String? = null
    ): CharacterResponse
}