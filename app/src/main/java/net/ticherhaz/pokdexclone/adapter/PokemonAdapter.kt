package net.ticherhaz.pokdexclone.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import net.ticherhaz.pokdexclone.databinding.ItemPokemonBinding
import net.ticherhaz.pokdexclone.model.PokemonList
import java.util.Locale
import javax.inject.Inject

class PokemonAdapter @Inject constructor(
    private val onPokemonClicked: (PokemonList) -> Unit,
    private val onIconFavouriteClicked: (PokemonList) -> Unit,
) :
    ListAdapter<PokemonList, PokemonAdapter.PokemonListViewHolder>(POKEMON_COMPARATOR) {

    class PokemonListViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonListViewHolder {
        val binding = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonListViewHolder, position: Int) {
        val pokemonList = getItem(position)

        with(holder.binding) {

            if (pokemonList.pokemonName.isNotBlank()) {
                tvPokemonName.text = pokemonList.pokemonName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }

            if (pokemonList.url.isNotBlank()) {
                progressBar.isVisible = true
                ivPokemon.isInvisible = true

                val url = "https://img.pokemondb.net/artwork/${pokemonList.pokemonName}.jpg"
                Glide.with(ivPokemon.context)
                    .load(url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.isVisible = false
                            ivPokemon.isVisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.isVisible = false
                            ivPokemon.isVisible = true
                            return false
                        }
                    })
                    .into(ivPokemon)
            }


            main.setOnClickListener {
                onPokemonClicked(pokemonList)
            }


            ivFavourite.setOnClickListener {

                onIconFavouriteClicked.invoke(pokemonList)
            }
        }
    }

    companion object {
        val POKEMON_COMPARATOR = object : DiffUtil.ItemCallback<PokemonList>() {
            override fun areItemsTheSame(oldItem: PokemonList, newItem: PokemonList): Boolean =
                oldItem.url == newItem.url

            override fun areContentsTheSame(oldItem: PokemonList, newItem: PokemonList): Boolean =
                oldItem == newItem
        }
    }
}