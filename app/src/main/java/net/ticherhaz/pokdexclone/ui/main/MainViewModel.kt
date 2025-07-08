package net.ticherhaz.pokdexclone.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ticherhaz.pokdexclone.model.PokemonList
import net.ticherhaz.pokdexclone.model.PokemonListResponse
import net.ticherhaz.pokdexclone.repository.AppRepository
import net.ticherhaz.pokdexclone.repository.IoDispatcher
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.utils.ConstantApi
import net.ticherhaz.pokdexclone.utils.QuickSave
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRepository: AppRepository,
    private val quickSave: QuickSave,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _pokemonListResponseStateFlow =
        MutableStateFlow<Resource<PokemonListResponse>>(Resource.Initialize())
    val pokemonListResponseStateFlow: StateFlow<Resource<PokemonListResponse>> =
        _pokemonListResponseStateFlow.asStateFlow()

    private val allPokemonList = mutableListOf<PokemonList>()

    companion object {
        const val TAG = "MainViewModel"
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Log.e(TAG, throwable.message ?: "Unknown error")
    }

    init {
        getPokemonList()
    }

    fun getPokemonList(offset: Int = 0) = viewModelScope.launch {
        _pokemonListResponseStateFlow.emit(Resource.Loading())
        val resource = withContext(ioDispatcher + coroutineExceptionHandler) {

            val decryptedUrlPathPokemonList =
                quickSave.decryptValue(ConstantApi.URL_PATH_POKEMON_LIST)
            appRepository.getPokemonList(
                urlPath = decryptedUrlPathPokemonList,
                limit = 20,
                offset = offset,
                context = context
            )
        }
        if (resource is Resource.Success && resource.data != null) {
            if (offset == 0) {
                allPokemonList.clear()
            }
            // Merge new items, preserving existing favorite states
            val existingNames = allPokemonList.map { it.pokemonName }.toSet()
            val newItems = resource.data.pokemonList.filter { it.pokemonName !in existingNames }
            allPokemonList.addAll(newItems)
            _pokemonListResponseStateFlow.emit(
                Resource.Success(
                    PokemonListResponse(
                        count = resource.data.count,
                        next = resource.data.next,
                        previous = resource.data.previous,
                        pokemonList = allPokemonList.toList()
                    )
                )
            )
        } else {
            _pokemonListResponseStateFlow.emit(resource)
        }
        Log.d(TAG, "Emitting resource: $resource, total items: ${allPokemonList.size}")
    }

    fun toggleFavorite(pokemonName: String, isFavourite: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            Log.d(TAG, "Toggling favorite for $pokemonName, current: $isFavourite")
            appRepository.setFavoritePokemon(pokemonName, !isFavourite)
            val updatedList = allPokemonList.map {
                if (it.pokemonName == pokemonName) {
                    it.copy(isFavourite = !it.isFavourite)
                } else {
                    it
                }
            }
            allPokemonList.clear()
            allPokemonList.addAll(updatedList)
            _pokemonListResponseStateFlow.emit(
                Resource.Success(
                    PokemonListResponse(
                        count = allPokemonList.size,
                        next = _pokemonListResponseStateFlow.value.let { if (it is Resource.Success) it.data?.next else null },
                        previous = _pokemonListResponseStateFlow.value.let { if (it is Resource.Success) it.data?.previous else null },
                        pokemonList = allPokemonList.toList()
                    )
                )
            )
            Log.d(
                TAG,
                "Emitted updated list with ${pokemonName}'s favorite status: ${!isFavourite}"
            )
        }
    }
}