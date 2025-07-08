package net.ticherhaz.pokdexclone.ui.main.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.databinding.ActivityPokemonDetailBinding
import net.ticherhaz.pokdexclone.model.PokemonDetail
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.ui.base.BaseActivity
import net.ticherhaz.pokdexclone.utils.DialogUtils
import net.ticherhaz.pokdexclone.utils.ProgressDialogCustom
import net.ticherhaz.pokdexclone.utils.Tools
import java.util.Locale

@AndroidEntryPoint
class PokemonDetailActivity : BaseActivity() {

    private val viewModel: PokemonDetailViewModel by viewModels()
    private lateinit var binding: ActivityPokemonDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        initWindowInsets()

        lifecycleScope.launch {
            viewModel.initIntent(intent)
        }
        setIbBack()

        startLifeCycle()

        setSivPokemon()
    }

    private fun startLifeCycle() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collectPokemonStateFlow()
                }
            }
        }
    }

    private suspend fun collectPokemonStateFlow() {
        viewModel.pokemonStateFlow.collect { resource ->
            when (resource) {
                is Resource.Initialize -> {
                }

                is Resource.Loading -> {
                    ProgressDialogCustom.show(this@PokemonDetailActivity)
                }

                is Resource.Success -> {
                    handleCollectPokemonSuccess(resource.data)
                }

                is Resource.Error -> {
                    ProgressDialogCustom.hide()
                    Tools.showToast(
                        this@PokemonDetailActivity,
                        resource.message ?: "Error loading data"
                    )
                }
            }
        }
    }

    private fun handleCollectPokemonSuccess(pokemonDetail: PokemonDetail?) {
        with(binding) {
            ProgressDialogCustom.hide()
            if (pokemonDetail == null) {
                Log.e("PokemonDetailActivity", "PokemonDetail data is null")
                Tools.showToast(this@PokemonDetailActivity, "No data available")
                return
            }

            val pokemon = pokemonDetail.pokemon
            if (pokemon.name.isNotBlank()) {
                tvPokemonName.text = pokemon.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            } else {
                tvPokemonName.text = getString(R.string.unknown)
            }

            tvWeight.text = pokemon.weight.toString()
            tvHeight.text = pokemon.height.toString()
            tvBaseExperience.text = pokemon.baseExperience.toString()

            llAbilities.removeAllViews()
            pokemon.abilities.forEach { ability ->
                val textView = TextView(this@PokemonDetailActivity)
                textView.text = ability.ability.name
                llAbilities.addView(textView)
            }

            // Load image
            val imageUrl = pokemonDetail.imageFilePath.ifBlank {
                "https://img.pokemondb.net/artwork/${pokemon.name}.jpg"
            }
            Log.d("PokemonDetailActivity", "Loading image for ${pokemon.name}: $imageUrl")

            Glide.with(this@PokemonDetailActivity)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                })
                .into(sivPokemon)
        }
    }

    private fun setSivPokemon() {
        binding.sivPokemon.setOnClickListener {
            DialogUtils.showDialogImage(
                context = this@PokemonDetailActivity,
                image = viewModel.pokemonStateFlow.value.data!!.imageFilePath,
                callback = object : DialogUtils.ShowDialogImageCallback {
                    override fun onImageFinishedLoaded() {
                    }
                }
            )
        }
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