package net.ticherhaz.pokdexclone.adapter

import android.graphics.drawable.Drawable
import android.util.Log
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
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.databinding.ItemPokemonBinding
import net.ticherhaz.pokdexclone.model.PokemonList
import java.util.Locale
import javax.inject.Inject

class PokemonAdapter @Inject constructor(
    private val onPokemonClicked: (PokemonList) -> Unit,
    private val onIconFavouriteClicked: (PokemonList) -> Unit,
) : ListAdapter<PokemonList, PokemonAdapter.PokemonListViewHolder>(POKEMON_COMPARATOR) {

    class PokemonListViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonListViewHolder {
        val binding = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonListViewHolder, position: Int) {
        val pokemonList = getItem(position)
        Log.d("PokemonAdapter", "Binding item at position $position: $pokemonList")

        with(holder.binding) {
            if (pokemonList.pokemonName.isNotBlank()) {
                tvPokemonName.text = pokemonList.pokemonName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            } else {
                tvPokemonName.text = tvPokemonName.context.getString(R.string.unknown)
            }

            ivFavourite.setImageResource(
                if (pokemonList.isFavourite) R.drawable.ic_favorite_filled_24dp
                else R.drawable.ic_favorite_24dp
            )
            Log.d(
                "PokemonAdapter",
                "Set favorite icon for ${pokemonList.pokemonName}: isFavourite=${pokemonList.isFavourite}"
            )

            if (pokemonList.url.isNotBlank()) {
                progressBar.isVisible = true
                ivPokemon.isInvisible = true

                val url = pokemonList.imageFilePath.ifBlank {
                    "https://img.pokemondb.net/artwork/${pokemonList.pokemonName}.jpg"
                }
                Log.d("PokemonAdapter", "Loading image for ${pokemonList.pokemonName}: $url")

                Glide.with(ivPokemon.context)
                    .load(url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e(
                                "PokemonAdapter",
                                "Image load failed for ${pokemonList.pokemonName}: ${e?.message}"
                            )
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
            } else {
                progressBar.isVisible = false
                ivPokemon.isVisible = true
            }

            main.setOnClickListener {
                onPokemonClicked(pokemonList)
            }

            ivFavourite.setOnClickListener {
                Log.d(
                    "PokemonAdapter",
                    "Favorite clicked for ${pokemonList.pokemonName}, current isFavourite: ${pokemonList.isFavourite}"
                )
                onIconFavouriteClicked(pokemonList)
            }
        }
    }

    companion object {
        val POKEMON_COMPARATOR = object : DiffUtil.ItemCallback<PokemonList>() {
            override fun areItemsTheSame(oldItem: PokemonList, newItem: PokemonList): Boolean =
                oldItem.url == newItem.url

            override fun areContentsTheSame(oldItem: PokemonList, newItem: PokemonList): Boolean {
                val result = oldItem == newItem
                Log.d(
                    "PokemonAdapter",
                    "DiffUtil contents same for ${oldItem.pokemonName}: $result (old: $oldItem, new: $newItem)"
                )
                return result
            }
        }
    }
}