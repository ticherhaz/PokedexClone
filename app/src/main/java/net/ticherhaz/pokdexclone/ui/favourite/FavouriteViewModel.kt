package net.ticherhaz.pokdexclone.ui.favourite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.ticherhaz.pokdexclone.model.PokemonList
import net.ticherhaz.pokdexclone.repository.AppRepository
import net.ticherhaz.pokdexclone.repository.IoDispatcher
import net.ticherhaz.pokdexclone.retrofit.Resource
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val appRepository: AppRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val favoritePokemonStateFlow: StateFlow<Resource<List<PokemonList>>> =
        appRepository.getFavoritePokemon()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Initialize()
            )

    companion object {
        const val TAG = "FavouriteListViewModel"
    }

    fun toggleFavorite(pokemonName: String, isFavourite: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            Log.d(TAG, "Toggling favorite for $pokemonName, current: $isFavourite")
            appRepository.setFavoritePokemon(pokemonName, !isFavourite)
        }
}