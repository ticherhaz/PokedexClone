package net.ticherhaz.pokdexclone.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_table")
data class PokemonEntity(
    @PrimaryKey val name: String,
    val url: String,
    val imageFilePath: String,
    var isFavourite: Boolean = false
)

// Mappers
fun PokemonListApi.toEntity(
    imageFilePath: String = "",
    isFavourite: Boolean = false
): PokemonEntity {
    return PokemonEntity(
        name = pokemonName,
        url = url,
        imageFilePath = imageFilePath,
        isFavourite = isFavourite
    )
}

fun PokemonEntity.toPokemonList(): PokemonList {
    return PokemonList(
        pokemonName = name,
        url = url,
        imageFilePath = imageFilePath,
        isFavourite = isFavourite
    )
}

fun List<PokemonEntity>.toPokemonListModel(): List<PokemonList> {
    return map { it.toPokemonList() }
}