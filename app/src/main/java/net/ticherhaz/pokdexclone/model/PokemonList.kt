package net.ticherhaz.pokdexclone.model

import com.google.gson.annotations.SerializedName
import java.util.Objects

data class PokemonListApi(
    @SerializedName("name")
    val pokemonName: String = "",
    @SerializedName("url")
    val url: String = ""
)

data class PokemonList(
    val pokemonName: String = "",
    val url: String = "",
    val imageFilePath: String = "",
    val isFavourite: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PokemonList) return false
        return pokemonName == other.pokemonName &&
                url == other.url &&
                imageFilePath == other.imageFilePath &&
                isFavourite == other.isFavourite
    }

    override fun hashCode(): Int {
        return Objects.hash(pokemonName, url, imageFilePath, isFavourite)
    }
}

data class PokemonListResponseApi(
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("next")
    val next: String? = null,
    @SerializedName("previous")
    val previous: String? = null,
    @SerializedName("results")
    val pokemonList: List<PokemonListApi> = emptyList()
)

data class PokemonListResponse(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val pokemonList: List<PokemonList> = emptyList()
)