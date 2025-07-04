package net.ticherhaz.pokdexclone

import android.app.Application
import com.google.android.material.color.ColorContrast
import com.google.android.material.color.ColorContrastOptions
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            initContrastColors()
        }
    }

    private fun initContrastColors() {
        val colorContrastOptions = ColorContrastOptions.Builder().apply {
            setHighContrastThemeOverlay(R.style.ThemeOverlay_AppTheme_HighContrast)
            setMediumContrastThemeOverlay(R.style.ThemeOverlay_AppTheme_MediumContrast)
        }

        ColorContrast.applyToActivitiesIfAvailable(this@MyApp, colorContrastOptions.build())
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}