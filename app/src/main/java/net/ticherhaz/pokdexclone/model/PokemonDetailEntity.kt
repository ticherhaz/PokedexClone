package net.ticherhaz.pokdexclone.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "pokemon_detail_table")
data class PokemonDetailEntity(
    @PrimaryKey val name: String,
    val weight: Int,
    val height: Int,
    val baseExperience: Int,
    val abilities: String // JSON string of ability names
)

// Mappers
fun Pokemon.toEntity(): PokemonDetailEntity {
    return PokemonDetailEntity(
        name = name,
        weight = weight,
        height = height,
        baseExperience = baseExperience,
        abilities = Gson().toJson(abilities.map { it.ability.name })
    )
}

fun PokemonDetailEntity.toPokemonDetail(imageFilePath: String = ""): PokemonDetail {
    val abilityNames = Gson().fromJson<List<String>>(
        abilities,
        object : TypeToken<List<String>>() {}.type
    )
    return PokemonDetail(
        pokemon = Pokemon(
            abilities = abilityNames.map {
                AbilitySlot(
                    NamedApiResource(name = it, url = ""),
                    false,
                    0
                )
            },
            baseExperience = baseExperience,
            cries = Cries("", ""),
            forms = emptyList(),
            gameIndices = emptyList(),
            height = height,
            heldItems = emptyList(),
            id = 0,
            isDefault = false,
            locationAreaEncounters = "",
            moves = emptyList(),
            name = name,
            order = 0,
            pastAbilities = emptyList(),
            pastTypes = emptyList(),
            species = NamedApiResource("", ""),
            sprites = Sprites(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                OtherSprites(
                    DreamWorldSprites(null, null),
                    HomeSprites(null, null, null, null),
                    OfficialArtworkSprites(null, null),
                    ShowdownSprites(null, null, null, null, null, null, null, null)
                ),
                VersionSprites(
                    GenerationISprites(
                        RedBlueSprites(null, null, null, null, null, null),
                        YellowSprites(null, null, null, null, null, null)
                    ),
                    GenerationIISprites(
                        CrystalSprites(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        ),
                        GoldSprites(null, null, null, null, null),
                        SilverSprites(null, null, null, null, null)
                    ),
                    GenerationIIISprites(
                        EmeraldSprites(null, null),
                        FireredLeafgreenSprites(null, null, null, null),
                        RubySapphireSprites(null, null, null, null)
                    ),
                    GenerationIVSprites(
                        DiamondPearlSprites(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        ),
                        HeartgoldSoulsilverSprites(null, null, null, null, null, null, null, null),
                        PlatinumSprites(null, null, null, null, null, null, null, null)
                    ),
                    GenerationVSprites(
                        BlackWhiteSprites(
                            AnimatedSprites(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                            ), null, null, null, null, null, null, null, null
                        )
                    ),
                    GenerationVISprites(
                        OmegarubyAlphasapphireSprites(null, null, null, null),
                        XYSprites(null, null, null, null)
                    ),
                    GenerationVIISprites(
                        IconSprites(null, null),
                        UltraSunUltraMoonSprites(null, null, null, null)
                    ),
                    GenerationVIIISprites(IconSprites(null, null))
                )
            ),
            stats = emptyList(),
            types = emptyList(),
            weight = weight
        ),
        imageFilePath = imageFilePath
    )
}