package net.ticherhaz.pokdexclone.model

data class Pokemon(
    val abilities: List<AbilitySlot>,
    val baseExperience: Int,
    val cries: Cries,
    val forms: List<NamedApiResource>,
    val gameIndices: List<GameIndex>,
    val height: Int,
    val heldItems: List<HeldItem>,
    val id: Int,
    val isDefault: Boolean,
    val locationAreaEncounters: String,
    val moves: List<MoveSlot>,
    val name: String,
    val order: Int,
    val pastAbilities: List<PastAbility>,
    val pastTypes: List<PastType>,
    val species: NamedApiResource,
    val sprites: Sprites,
    val stats: List<StatSlot>,
    val types: List<TypeSlot>,
    val weight: Int
)

data class PokemonDetail(
    val pokemon: Pokemon,
    val imageFilePath: String = ""
)

data class AbilitySlot(
    val ability: NamedApiResource,
    val isHidden: Boolean,
    val slot: Int
)

data class Cries(
    val latest: String,
    val legacy: String
)

data class GameIndex(
    val gameIndex: Int,
    val version: NamedApiResource
)

data class HeldItem(
    val item: NamedApiResource,
    val versionDetails: List<VersionDetail>
)

data class VersionDetail(
    val rarity: Int,
    val version: NamedApiResource
)

data class MoveSlot(
    val move: NamedApiResource,
    val versionGroupDetails: List<VersionGroupDetail>
)

data class VersionGroupDetail(
    val levelLearnedAt: Int,
    val moveLearnMethod: NamedApiResource,
    val order: Int?,
    val versionGroup: NamedApiResource
)

data class PastAbility(
    val abilities: List<AbilitySlot>,
    val generation: NamedApiResource
)

data class PastType(
    val generation: NamedApiResource,
    val types: List<TypeSlot>
)

data class Sprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?,
    val other: OtherSprites,
    val versions: VersionSprites
)

data class OtherSprites(
    val dreamWorld: DreamWorldSprites,
    val home: HomeSprites,
    val officialArtwork: OfficialArtworkSprites,
    val showdown: ShowdownSprites
)

data class DreamWorldSprites(
    val frontDefault: String?,
    val frontFemale: String?
)

data class HomeSprites(
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class OfficialArtworkSprites(
    val frontDefault: String?,
    val frontShiny: String?
)

data class ShowdownSprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class VersionSprites(
    val generationI: GenerationISprites,
    val generationII: GenerationIISprites,
    val generationIII: GenerationIIISprites,
    val generationIV: GenerationIVSprites,
    val generationV: GenerationVSprites,
    val generationVI: GenerationVISprites,
    val generationVII: GenerationVIISprites,
    val generationVIII: GenerationVIIISprites
)

data class GenerationISprites(
    val redBlue: RedBlueSprites,
    val yellow: YellowSprites
)

data class RedBlueSprites(
    val backDefault: String?,
    val backGray: String?,
    val backTransparent: String?,
    val frontDefault: String?,
    val frontGray: String?,
    val frontTransparent: String?
)

data class YellowSprites(
    val backDefault: String?,
    val backGray: String?,
    val backTransparent: String?,
    val frontDefault: String?,
    val frontGray: String?,
    val frontTransparent: String?
)

data class GenerationIISprites(
    val crystal: CrystalSprites,
    val gold: GoldSprites,
    val silver: SilverSprites
)

data class CrystalSprites(
    val backDefault: String?,
    val backShiny: String?,
    val backShinyTransparent: String?,
    val backTransparent: String?,
    val frontDefault: String?,
    val frontShiny: String?,
    val frontShinyTransparent: String?,
    val frontTransparent: String?
)

data class GoldSprites(
    val backDefault: String?,
    val backShiny: String?,
    val frontDefault: String?,
    val frontShiny: String?,
    val frontTransparent: String?
)

data class SilverSprites(
    val backDefault: String?,
    val backShiny: String?,
    val frontDefault: String?,
    val frontShiny: String?,
    val frontTransparent: String?
)

data class GenerationIIISprites(
    val emerald: EmeraldSprites,
    val fireredLeafgreen: FireredLeafgreenSprites,
    val rubySapphire: RubySapphireSprites
)

data class EmeraldSprites(
    val frontDefault: String?,
    val frontShiny: String?
)

data class FireredLeafgreenSprites(
    val backDefault: String?,
    val backShiny: String?,
    val frontDefault: String?,
    val frontShiny: String?
)

data class RubySapphireSprites(
    val backDefault: String?,
    val backShiny: String?,
    val frontDefault: String?,
    val frontShiny: String?
)

data class GenerationIVSprites(
    val diamondPearl: DiamondPearlSprites,
    val heartgoldSoulsilver: HeartgoldSoulsilverSprites,
    val platinum: PlatinumSprites
)

data class DiamondPearlSprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class HeartgoldSoulsilverSprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class PlatinumSprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class GenerationVSprites(
    val blackWhite: BlackWhiteSprites
)

data class BlackWhiteSprites(
    val animated: AnimatedSprites,
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class AnimatedSprites(
    val backDefault: String?,
    val backFemale: String?,
    val backShiny: String?,
    val backShinyFemale: String?,
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class GenerationVISprites(
    val omegarubyAlphasapphire: OmegarubyAlphasapphireSprites,
    val xY: XYSprites
)

data class OmegarubyAlphasapphireSprites(
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class XYSprites(
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class GenerationVIISprites(
    val icons: IconSprites,
    val ultraSunUltraMoon: UltraSunUltraMoonSprites
)

data class IconSprites(
    val frontDefault: String?,
    val frontFemale: String?
)

data class UltraSunUltraMoonSprites(
    val frontDefault: String?,
    val frontFemale: String?,
    val frontShiny: String?,
    val frontShinyFemale: String?
)

data class GenerationVIIISprites(
    val icons: IconSprites
)

data class StatSlot(
    val baseStat: Int,
    val effort: Int,
    val stat: NamedApiResource
)

data class TypeSlot(
    val slot: Int,
    val type: NamedApiResource
)

data class NamedApiResource(
    val name: String,
    val url: String
)