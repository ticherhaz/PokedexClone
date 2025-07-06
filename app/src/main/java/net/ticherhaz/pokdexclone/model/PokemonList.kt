package net.ticherhaz.pokdexclone.model

import com.google.gson.annotations.SerializedName


data class PokemonList(
    @SerializedName("name")
    val pokemonName: String = "",
    @SerializedName("url")
    val url: String = ""
)

data class PokemonListResponse(
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("next")
    val next: String? = null,
    @SerializedName("previous")
    val previous: String? = null,
    @SerializedName("results")
    val pokemonList: List<PokemonList> = emptyList()
)