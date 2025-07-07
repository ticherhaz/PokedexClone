package net.ticherhaz.pokdexclone.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.ticherhaz.pokdexclone.model.PokemonEntity

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemon: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    @Update
    suspend fun updatePokemon(pokemon: PokemonEntity)

    @Query("SELECT * FROM pokemon_table ORDER BY name ASC")
    fun getAllPokemon(): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_table WHERE name = :name LIMIT 1")
    suspend fun getPokemonByName(name: String): PokemonEntity?

    @Query("DELETE FROM pokemon_table")
    suspend fun clearAllPokemon()

    @Query("SELECT * FROM pokemon_table ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getPokemonPaginated(limit: Int, offset: Int): Flow<List<PokemonEntity>>

    @Query("UPDATE pokemon_table SET isFavourite = :isFavourite WHERE name = :pokemonName")
    suspend fun setFavorite(pokemonName: String, isFavourite: Boolean)
}