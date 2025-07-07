package net.ticherhaz.pokdexclone.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream

object ImageCacheManager {
    /**
     * Downloads and caches an image from URL, returns local file path
     */
    fun cacheImage(context: Context, imageUrl: String, pokemonName: String): String? {
        // Create a unique filename for the cached image
        val fileName = "pokemon_${pokemonName.lowercase().replace(" ", "_")}.png"
        val cacheFile = File(context.cacheDir, fileName)

        // If file already exists, return its path
        if (cacheFile.exists()) {
            return cacheFile.absolutePath
        }

        return try {
            // Use Glide to download the image
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get() // This runs on a background thread because it's in a suspend function

            // Save the bitmap to cache file
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            cacheFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Clears cached images when needed (e.g., on logout or when clearing app data)
     */
    fun clearCache(context: Context) {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("pokemon_")) {
                file.delete()
            }
        }
    }
}