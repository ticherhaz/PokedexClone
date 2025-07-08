package net.ticherhaz.pokdexclone.ui.favourite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.adapter.PokemonAdapter
import net.ticherhaz.pokdexclone.databinding.ActivityFavouriteBinding
import net.ticherhaz.pokdexclone.model.PokemonList
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.ui.base.BaseActivity
import net.ticherhaz.pokdexclone.ui.main.detail.PokemonDetailActivity
import net.ticherhaz.pokdexclone.utils.Constant
import net.ticherhaz.pokdexclone.utils.ProgressDialogCustom
import net.ticherhaz.pokdexclone.utils.Tools

@AndroidEntryPoint
class FavouriteActivity : BaseActivity() {
    private val viewModel: FavouriteViewModel by viewModels()
    private lateinit var binding: ActivityFavouriteBinding

    private val pokemonAdapter: PokemonAdapter by lazy {
        PokemonAdapter(
            onPokemonClicked = ::handleOnPokemonClicked,
            onIconFavouriteClicked = ::handleOnIconFavouriteClicked
        )
    }

    private fun handleOnPokemonClicked(pokemonList: PokemonList) {
        val intentPokemon = Intent(this, PokemonDetailActivity::class.java)
        intentPokemon.putExtra(Constant.POKEMON_NAME, pokemonList.pokemonName)
        startActivity(intentPokemon)
    }

    private fun handleOnIconFavouriteClicked(pokemonList: PokemonList) {
        Log.d(
            "FavouriteActivity",
            "Favorite clicked for ${pokemonList.pokemonName}, current isFavourite: ${pokemonList.isFavourite}"
        )
        viewModel.toggleFavorite(
            pokemonName = pokemonList.pokemonName,
            isFavourite = pokemonList.isFavourite
        )
        Tools.vibrate(this)
        Tools.showToast(
            this,
            if (!pokemonList.isFavourite) "Saved to Favourite" else "Removed from Favourite"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWindowInsets()

        setIbBack()

        initPokemonAdapter()
        startLifeCycle()
    }

    private fun initPokemonAdapter() {
        binding.recycleViewPokemon.apply {
            adapter = pokemonAdapter
        }
    }

    private fun startLifeCycle() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collectPokemonListResponseStateFlow()
                }
            }
        }
    }

    private suspend fun collectPokemonListResponseStateFlow() {
        viewModel.favoritePokemonStateFlow.collect { resource ->
            when (resource) {
                is Resource.Initialize -> {
                    Log.d("FavouriteActivity", "State: Initialize")
                }

                is Resource.Loading -> {
                    Log.d("FavouriteActivity", "State: Loading")
                    ProgressDialogCustom.show(this@FavouriteActivity)
                }

                is Resource.Success -> {
                    Log.d(
                        "FavouriteActivity",
                        "State: Success, data: ${resource.data?.size} items"
                    )
                    handleCollectPokemonListSuccess(resource.data)
                }

                is Resource.Error -> {
                    Log.e("FavouriteActivity", "State: Error, message: ${resource.message}")
                    ProgressDialogCustom.hide()
                    Tools.showToast(
                        this@FavouriteActivity,
                        resource.message ?: "Error loading data"
                    )
                }
            }
        }
    }

    private fun handleCollectPokemonListSuccess(pokemonList: List<PokemonList>?) {
        ProgressDialogCustom.hide()

        if (pokemonList == null) {
            Log.e("FavouriteActivity", "PokemonListResponse is null")
            return
        }
        pokemonAdapter.submitList(pokemonList.toList())
    }

    private fun setIbBack() {
        binding.ibBack.setOnClickListener {
            finish()
        }
    }

    private fun initWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}