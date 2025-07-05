package net.ticherhaz.pokdexclone.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshEventBus @Inject constructor() {
    private val _refreshEvents = MutableSharedFlow<Unit>(
        replay = 1  // Keep 1 event for late subscribers
    )
    val refreshEvents: SharedFlow<Unit> = _refreshEvents

    suspend fun notifyRefresh() {
        _refreshEvents.emit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearCache() {
        _refreshEvents.resetReplayCache()  // Clear cached events
    }
}