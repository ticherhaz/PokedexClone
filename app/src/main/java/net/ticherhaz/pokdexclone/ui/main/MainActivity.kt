package net.ticherhaz.pokdexclone.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.adapter.PokemonAdapter
import net.ticherhaz.pokdexclone.databinding.ActivityMainBinding
import net.ticherhaz.pokdexclone.model.PokemonList
import net.ticherhaz.pokdexclone.model.PokemonListResponse
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.ui.base.BaseActivity
import net.ticherhaz.pokdexclone.utils.ProgressDialogCustom
import net.ticherhaz.pokdexclone.utils.Tools

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var offset = 0
    private var isLoading = false

    private val pokemonAdapter: PokemonAdapter by lazy {
        PokemonAdapter(
            onPokemonClicked = ::handleOnPokemonClicked,
            onIconFavouriteClicked = ::handleOnIconFavouriteClicked
        )
    }

    private fun handleOnPokemonClicked(pokemonList: PokemonList) {
        Log.d("MainActivity", "Pokemon clicked: ${pokemonList.pokemonName}")
    }

    private fun handleOnIconFavouriteClicked(pokemonList: PokemonList) {
        Log.d(
            "MainActivity",
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initPlayerAdapter()
        initRecycleViewOnScrollListener()
        startLifeCycle()
    }

    private fun initPlayerAdapter() {
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
        viewModel.pokemonListResponseStateFlow.collect { resource ->
            when (resource) {
                is Resource.Initialize -> {
                    Log.d("MainActivity", "State: Initialize")
                }

                is Resource.Loading -> {
                    Log.d("MainActivity", "State: Loading")
                    ProgressDialogCustom.show(this@MainActivity)
                }

                is Resource.Success -> {
                    Log.d(
                        "MainActivity",
                        "State: Success, data: ${resource.data?.pokemonList?.size} items"
                    )
                    handleCollectPokemonListSuccess(resource.data)
                }

                is Resource.Error -> {
                    Log.e("MainActivity", "State: Error, message: ${resource.message}")
                    ProgressDialogCustom.hide()
                    Tools.showToast(this@MainActivity, resource.message ?: "Error loading data")
                }
            }
        }
    }

    private fun handleCollectPokemonListSuccess(pokemonListResponse: PokemonListResponse?) {
        isLoading = false
        ProgressDialogCustom.hide()

        if (pokemonListResponse == null) {
            Log.e("MainActivity", "PokemonListResponse is null")
            return
        }

        Log.d(
            "MainActivity",
            "Submitting list size: ${pokemonListResponse.pokemonList.size}, items: ${pokemonListResponse.pokemonList}"
        )
        pokemonAdapter.submitList(pokemonListResponse.pokemonList.toList())
    }

    private fun initRecycleViewOnScrollListener() {
        binding.recycleViewPokemon.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    layoutManager?.let {
                        val visibleItemCount = it.childCount
                        val totalItemCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount > 0) {
                            isLoading = true
                            offset += 20
                            Log.d("MainActivity", "Loading more, offset: $offset")
                            lifecycleScope.launch {
                                viewModel.getPokemonList(offset = offset)
                            }
                        }
                    }
                }
            }
        })
    }
}