package net.ticherhaz.pokdexclone.repository

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.ticherhaz.pokdexclone.dao.AppDatabase
import net.ticherhaz.pokdexclone.dao.PokemonDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // For application-wide singletons
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pokemon_database"
        ).build()
    }


    @Provides
    // No need for @Singleton here if AppDatabase is Singleton, as DAO comes from it
    fun providePokemonDao(appDatabase: AppDatabase): PokemonDao {
        return appDatabase.pokemonDao()
    }
}