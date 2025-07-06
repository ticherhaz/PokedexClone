package net.ticherhaz.pokdexclone.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {
    // Safely collect flows tied to activity lifecycle
    protected inline fun <T> collectLifecycleAwareFlow(
        flow: Flow<T>,
        crossinline collector: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { value ->
                    collector(value)
                }
            }
        }
    }
}