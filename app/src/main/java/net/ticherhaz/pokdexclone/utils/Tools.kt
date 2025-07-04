package net.ticherhaz.pokdexclone.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Process
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import net.ticherhaz.pokdexclone.R

object Tools {

    fun showToast(context: Context?, message: String?) {
        val toast: Toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    fun logSimple(message: String?) {
        Log.i("???", "$message")
    }

    fun showImageGlide(context: Context, imageUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun vibrate(context: Context) {
        // Use the recommended way to get the Vibrator system service
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For API 31 (S) and above, use VibratorManager
            val vibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            // For APIs below 31, use getSystemService(Context.VIBRATOR_SERVICE)
            @Suppress("DEPRECATION") // Suppress the deprecation warning for older APIs
            context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }

        // Check if a vibrator was successfully obtained before attempting to vibrate
        vibrator?.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun RecyclerView.smoothSnapToPosition(
        position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START
    ) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    fun RecyclerView.immediateSnapToPosition() {
        isVisible = false
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollToPosition(0)
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                isVisible = true
            }
        })
    }

    fun Context.simpleCopyText(view: View, key: String, value: String) {
        vibrate(this)
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(key, value)
        clipboard.setPrimaryClip(clip)
        val copied = getString(R.string.copied)
        Snackbar.make(view, "$copied $key", Snackbar.LENGTH_LONG).show()
    }


    fun Context.restartAppFromFresh() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        // Kill current process (optional, ensures full cleanup)
        Process.killProcess(Process.myPid())
    }
}