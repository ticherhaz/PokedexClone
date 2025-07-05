package net.ticherhaz.pokdexclone.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.enumerator.HttpCode
import net.ticherhaz.pokdexclone.model.PokemonListResponse
import net.ticherhaz.pokdexclone.repository.AppRepository
import net.ticherhaz.pokdexclone.repository.IoDispatcher
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.utils.ConstantApi
import net.ticherhaz.pokdexclone.utils.QuickSave
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val quickSave: QuickSave,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    /**
     *  We use StateFlow instead of SharedFlow because we want to use advantage of ScreenRotate.
     *  If use SharedFlow, need to use replay = 1 . not 0
     */
    private val _pokemonListResponseStateFlow =
        MutableStateFlow<Resource<PokemonListResponse>>(Resource.Initialize())
    val pokemonListResponseStateFlow: StateFlow<Resource<PokemonListResponse>> =
        _pokemonListResponseStateFlow

    companion object {
        const val TAG = " MainViewModel"
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        Log.e(TAG, throwable.message + "")
    }

    init {
        getPokemonList()
    }

    fun getPokemonList(offset: Int = 0) =
        viewModelScope.launch {
            _pokemonListResponseStateFlow.emit(Resource.Loading())
            try {
                val data = withContext(ioDispatcher + coroutineExceptionHandler) {
                    val limit = 20
                    val response = appRepository.getPokemonList(
                        urlPath = ConstantApi.URL_PATH_POKEMON_LIST,
                        limit = limit,
                        offset = offset
                    )
                    Resource.getResponse { response }
                }

                return@launch _pokemonListResponseStateFlow.emit(data)
            } catch (e: Exception) {

                when (e) {
                    is IOException -> {

                        //Compulsory
                        val errorMessage = e.message
                        if (errorMessage != null && errorMessage.contains("failed to connect to")) {
                            return@launch _pokemonListResponseStateFlow.emit(
                                Resource.Error(
                                    code = HttpCode.RETRY_TO_CONNECT.code,
                                    messageInt = HttpCode.RETRY_TO_CONNECT.messageInt
                                )
                            )
                        }

                        return@launch _pokemonListResponseStateFlow.emit(
                            Resource.Error(
                                code = 12029,
                                messageInt = R.string.internet_connection_problem
                            )
                        )

                    }

                    else -> {
                        return@launch _pokemonListResponseStateFlow.emit(
                            Resource.Error(
                                code = 700,
                                message = "Player List Error: " + e.message
                            )
                        )
                    }
                }
            }
        }
}