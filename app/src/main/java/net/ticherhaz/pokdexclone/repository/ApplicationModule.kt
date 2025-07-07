package net.ticherhaz.pokdexclone.repository

import android.content.Context
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.ticherhaz.pokdexclone.dao.PokemonDao
import net.ticherhaz.pokdexclone.service.NetworkMonitor
import net.ticherhaz.pokdexclone.service.NetworkMonitorImpl
import net.ticherhaz.pokdexclone.utils.QuickSave
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    fun provideQuickSave(): QuickSave = QuickSave.getInstance()

    @Provides
    fun provideAppRepository(
        pokemonDao: PokemonDao
    ): AppRepository = AppRepository(pokemonDao = pokemonDao)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideRefreshEventBus(): RefreshEventBus = RefreshEventBus()

    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        connectivityManager: ConnectivityManager
    ): NetworkMonitor {
        return NetworkMonitorImpl(connectivityManager)
    }
}