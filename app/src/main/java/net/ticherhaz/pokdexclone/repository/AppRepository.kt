package net.ticherhaz.pokdexclone.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first
import net.ticherhaz.pokdexclone.dao.PokemonDao
import net.ticherhaz.pokdexclone.model.PokemonDetail
import net.ticherhaz.pokdexclone.model.PokemonListResponse
import net.ticherhaz.pokdexclone.model.toEntity
import net.ticherhaz.pokdexclone.model.toPokemonDetail
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

    suspend fun setFavoritePokemon(pokemonName: String, isFavourite: Boolean) {
        pokemonDao.setFavorite(pokemonName = pokemonName, isFavourite = isFavourite)
    }

    private fun extractImageUrlFromPokemonUrl(pokemonUrl: String): String? {
        val id = pokemonUrl.split("/").filter { it.isNotEmpty() }.lastOrNull()
        return id?.toIntOrNull()?.let {
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$it.png"
        }
    }

    suspend fun getPokemonDetail(urlPath: String): Resource<PokemonDetail> {
        try {
            val pokemonName = urlPath.split("/").last()
            val cachedDetail = pokemonDao.getPokemonDetailByName(pokemonName)
            val cachedEntity = pokemonDao.getPokemonByName(pokemonName)
            val imageFilePath = cachedEntity?.imageFilePath ?: ""

            if (cachedDetail != null) {
                Log.d("AppRepository", "Returning cached Pokemon detail for $pokemonName")
                return Resource.Success(
                    cachedDetail.toPokemonDetail(imageFilePath),
                    isFromCache = true
                )
            }

            val response = apiInterface.getPokemonDetail(urlPath = urlPath)
            Log.d(
                "AppRepository",
                "Pokemon detail API response: $response, body: ${response.body()}"
            )
            if (response.isSuccessful) {
                val pokemon = response.body()
                if (pokemon != null) {
                    pokemonDao.insertPokemonDetail(pokemon.toEntity())
                    return Resource.Success(PokemonDetail(pokemon, imageFilePath))
                } else {
                    return Resource.Error(code = response.code(), message = "Empty response body")
                }
            } else {
                return Resource.Error(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Pokemon detail exception: ${e.message}", e)
            val pokemonName = urlPath.split("/").last()
            val cachedDetail = pokemonDao.getPokemonDetailByName(pokemonName)
            val cachedEntity = pokemonDao.getPokemonByName(pokemonName)
            val imageFilePath = cachedEntity?.imageFilePath ?: ""
            if (cachedDetail != null) {
                Log.d("AppRepository", "Returning cached Pokemon detail for $pokemonName")
                return Resource.Success(
                    cachedDetail.toPokemonDetail(imageFilePath),
                    isFromCache = true
                )
            }
            return Resource.Error(message = e.message ?: "An unknown error occurred")
        }
    }
}