package net.ticherhaz.pokdexclone.repository

import net.ticherhaz.pokdexclone.retrofit.ApiInterface
import net.ticherhaz.pokdexclone.retrofit.RetrofitClient


class AppRepository(private val apiInterface: ApiInterface = RetrofitClient.apiInterface) {
    suspend fun getPokemonList(urlPath: String, limit: Int, offset: Int) =
        apiInterface.getPokemonList(
            urlPath = urlPath,
            limit = limit,
            offset = offset
        )
}