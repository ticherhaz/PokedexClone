package net.ticherhaz.pokdexclone.retrofit

import net.ticherhaz.pokdexclone.model.PokemonListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiInterface {

    /**
     *  encoded = true , if not put the url slash / will change to %2F
     *  Refer: https://stackoverflow.com/a/74037210/28408518
     */

    @GET("{url_path}")
    suspend fun getPokemonList(
        @Path("url_path", encoded = true) urlPath: String = "",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PokemonListResponse>
}