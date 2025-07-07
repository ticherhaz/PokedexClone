package net.ticherhaz.pokdexclone.ui.main.detail

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ticherhaz.pokdexclone.model.PokemonDetail
import net.ticherhaz.pokdexclone.repository.AppRepository
import net.ticherhaz.pokdexclone.repository.IoDispatcher
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.utils.Constant
import net.ticherhaz.pokdexclone.utils.ConstantApi
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _pokemonStateFlow =
        MutableStateFlow<Resource<PokemonDetail>>(Resource.Initialize())
    val pokemonStateFlow = _pokemonStateFlow.asStateFlow()

    private val _favouritePokemonSharedFlow =
        MutableSharedFlow<Resource<Boolean>>(replay = 1)
    val favouritePokemonSharedFlow = _favouritePokemonSharedFlow.asSharedFlow()

    companion object {
        const val TAG = "PokemonDetailViewModel"
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Log.e(TAG, throwable.message ?: "Unknown error")
    }

    private var pokemonName: String = ""

    fun initIntent(intent: Intent) {
        val pokemonName = intent.getStringExtra(Constant.POKEMON_NAME)
        if (pokemonName != null) {
            this.pokemonName = pokemonName
            getPokemonDetail(pokemonName)
        } else {
            Log.e(TAG, "Pokemon name not found in intent")
            viewModelScope.launch {
                _pokemonStateFlow.emit(Resource.Error(message = "Pokemon name not provided"))
            }
        }
    }

    fun getPokemonDetail(pokemonName: String) = viewModelScope.launch {
        _pokemonStateFlow.emit(Resource.Loading())
        val resource = withContext(ioDispatcher + coroutineExceptionHandler) {
            val urlPath = "${ConstantApi.URL_PATH_POKEMON_LIST}/$pokemonName"
            appRepository.getPokemonDetail(urlPath = urlPath)
        }
        Log.d(TAG, "Emitting Pokemon detail: $resource")
        _pokemonStateFlow.emit(resource)
    }
}