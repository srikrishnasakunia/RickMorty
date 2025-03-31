package dev.krishna.rickmorty.data.api

import dev.krishna.rickmorty.data.api.model.CharacterResponse
import dev.krishna.rickmorty.data.api.model.Episode
import dev.krishna.rickmorty.data.api.model.RickMortyCharacter
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("character/{id}")
    suspend fun getCharacterDetails(
        @Path("id")
        id: Int
    ): RickMortyCharacter

    @GET("episode/{ids}")
    suspend fun getMultipleEpisodes(
        @Path("ids") ids: String
    ): List<Episode>

    @GET("episode/{ids}")
    suspend fun getSingleEpisodes(
        @Path("ids") ids: String
    ): Episode
}