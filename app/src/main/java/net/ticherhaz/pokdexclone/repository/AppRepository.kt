package net.ticherhaz.pokdexclone.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first
import net.ticherhaz.pokdexclone.dao.PokemonDao
import net.ticherhaz.pokdexclone.model.PokemonListResponse
import net.ticherhaz.pokdexclone.model.toEntity
import net.ticherhaz.pokdexclone.model.toPokemonListModel
import net.ticherhaz.pokdexclone.retrofit.ApiInterface
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.retrofit.RetrofitClient
import net.ticherhaz.pokdexclone.utils.ImageCacheManager
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val apiInterface: ApiInterface = RetrofitClient.apiInterface,
    private val pokemonDao: PokemonDao
) {
    suspend fun getPokemonList(
        urlPath: String,
        limit: Int,
        offset: Int,
        context: Context
    ): Resource<PokemonListResponse> {
        try {
            val response =
                apiInterface.getPokemonList(urlPath = urlPath, limit = limit, offset = offset)
            Log.d("AppRepository", "API response: $response, body: ${response.body()}")
            if (response.isSuccessful) {
                val pokemonListResponse = response.body()
                if (pokemonListResponse != null) {
                    Log.d(
                        "AppRepository",
                        "API pokemonList size: ${pokemonListResponse.pokemonList.size}"
                    )
                    val pokemonEntities = pokemonListResponse.pokemonList.map { pokemon ->
                        val imageUrl = extractImageUrlFromPokemonUrl(pokemon.url)
                        val imagePath = imageUrl?.let {
                            ImageCacheManager.cacheImage(context, it, pokemon.pokemonName)
                        } ?: ""
                        pokemon.toEntity(
                            imageFilePath = imagePath,
                            isFavourite = pokemonDao.getPokemonByName(pokemon.pokemonName)?.isFavourite
                                ?: false
                        )
                    }

                    pokemonDao.insertPokemonList(pokemonEntities)
                    val uiPokemonList = pokemonEntities.toPokemonListModel()
                    Log.d("AppRepository", "UI pokemonList size: ${uiPokemonList.size}")

                    return Resource.Success(
                        PokemonListResponse(
                            count = pokemonListResponse.count,
                            next = pokemonListResponse.next,
                            previous = pokemonListResponse.previous,
                            pokemonList = uiPokemonList
                        )
                    )
                } else {
                    Log.e("AppRepository", "Empty response body")
                    return Resource.Error(code = response.code(), message = "Empty response body")
                }
            } else {
                Log.e("AppRepository", "API failed: ${response.code()}, ${response.message()}")
                if (offset == 0) {
                    val cachedPokemon = pokemonDao.getPokemonPaginated(limit, offset).first()
                    Log.d("AppRepository", "Cached pokemon size: ${cachedPokemon.size}")
                    if (cachedPokemon.isNotEmpty()) {
                        val uiPokemonList = cachedPokemon.toPokemonListModel()
                        val responseFromCache = PokemonListResponse(
                            count = cachedPokemon.size,
                            next = null,
                            previous = null,
                            pokemonList = uiPokemonList
                        )
                        return Resource.Success(responseFromCache, isFromCache = true)
                    }
                }
                return Resource.Error(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Exception: ${e.message}", e)
            if (offset == 0) {
                try {
                    val cachedPokemon = pokemonDao.getPokemonPaginated(limit, offset).first()
                    Log.d("AppRepository", "Cached pokemon size: ${cachedPokemon.size}")
                    if (cachedPokemon.isNotEmpty()) {
                        val uiPokemonList = cachedPokemon.toPokemonListModel()
                        val responseFromCache = PokemonListResponse(
                            count = cachedPokemon.size,
                            next = null,
                            previous = null,
                            pokemonList = uiPokemonList
                        )
                        return Resource.Success(responseFromCache, isFromCache = true)
                    }
                } catch (e: Exception) {
                    Log.e("AppRepository", "Cache fetch failed: ${e.message}")
                }
            }
            return Resource.Error(message = e.message ?: "An unknown error occurred")
        }
    }

    suspend fun setFavoritePokemon(pokemonName: String, isFavorite: Boolean) {
        pokemonDao.setFavorite(pokemonName, isFavorite)
        Log.d("AppRepository", "Set favorite for $pokemonName to $isFavorite")
    }

    private fun extractImageUrlFromPokemonUrl(pokemonUrl: String): String? {
        val id = pokemonUrl.split("/").filter { it.isNotEmpty() }.lastOrNull()
        return id?.toIntOrNull()?.let {
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$it.png"
        }
    }
}