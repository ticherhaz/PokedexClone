package net.ticherhaz.pokdexclone.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.ticherhaz.pokdexclone.model.PokemonEntity

@Database(
    entities = [PokemonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile // volatile to make sure the value is always up-to-date and the same for all execution threads
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // synchronized to make it thread-safe
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokedex_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}