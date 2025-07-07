package net.ticherhaz.pokdexclone.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import net.ticherhaz.pokdexclone.model.PokemonDetailEntity
import net.ticherhaz.pokdexclone.model.PokemonEntity

@Database(
    entities = [PokemonEntity::class, PokemonDetailEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}