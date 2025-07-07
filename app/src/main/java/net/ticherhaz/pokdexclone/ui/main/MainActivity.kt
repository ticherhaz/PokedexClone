package net.ticherhaz.pokdexclone.ui.main

import android.os.Bundle
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

    private val pokemonAdapter: PokemonAdapter by lazy {
        PokemonAdapter(
            onPokemonClicked = ::handleOnPokemonClicked,
            onIconFavouriteClicked = ::handleOnIconFavouriteClicked
        )
    }

    private fun handleOnPokemonClicked(pokemonList: PokemonList) {

    }

    private fun handleOnIconFavouriteClicked(pokemonList: PokemonList) {
        Tools.vibrate(this)
        Tools.showToast(this, "Saved to Favourite")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initRecycleViewOnScrollListener()
        initPlayerAdapter()

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
        viewModel.pokemonListResponseStateFlow.collect {
            when (it) {
                is Resource.Initialize -> {
                }

                is Resource.Loading -> {
                    ProgressDialogCustom.show(this@MainActivity)
                }

                is Resource.Success -> {
                    handleCollectPokemonListSuccess(it.data)
                }

                is Resource.Error -> {
                    ProgressDialogCustom.hide()
                }
            }
        }
    }

    private var isLoading = false
    private fun handleCollectPokemonListSuccess(pokemonListResponse: PokemonListResponse?) {
        isLoading = false // <--- SET isLoading to false HERE

        if (pokemonListResponse == null) return

        // If it's the first load (offset is 0), replace the list.
        // Otherwise, append to the existing list.
        val currentList = if (offset == 0) {
            pokemonListResponse.pokemonList
        } else {
            // Make sure pokemonAdapter.currentList is not modified directly if it's immutable
            // Create a new list by combining the old and new items
            val oldList = pokemonAdapter.currentList
            oldList + pokemonListResponse.pokemonList
        }
        pokemonAdapter.submitList(currentList)
        ProgressDialogCustom.hide() // Hide progress dialog
    }

    private fun initRecycleViewOnScrollListener() {
        binding.recycleViewPokemon.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // Only check when scrolling down
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    layoutManager?.let {
                        val visibleItemCount = it.childCount
                        val totalItemCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()

                        // Check if not currently loading, and if the last item is visible
                        if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount > 0) {
                            // Check totalItemCount > 0 to avoid loading when the list is empty initially
                            // and the scroll condition might technically be met.

                            isLoading = true // <--- SET isLoading to true BEFORE making the call
                            offset += 20

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