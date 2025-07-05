package net.ticherhaz.pokdexclone.service

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkMonitor {
    suspend fun isOnline(): Boolean
}

@Singleton
class NetworkMonitorImpl @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : NetworkMonitor {

    override suspend fun isOnline(): Boolean {
        return withContext(Dispatchers.IO) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }
    }
}