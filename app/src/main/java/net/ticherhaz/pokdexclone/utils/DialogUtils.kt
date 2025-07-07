package net.ticherhaz.pokdexclone.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import net.ticherhaz.pokdexclone.R

object DialogUtils {

    fun showDialogImage(
        context: Context,
        image: String,
        callback: ShowDialogImageCallback
    ) {
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image)

        dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)

        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT


        val ivMain = dialog.findViewById<ImageView>(R.id.iv_main)
        Glide.with(context)
            .load(image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    callback.onImageFinishedLoaded()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    callback.onImageFinishedLoaded()
                    return false
                }

            })
            .into(ivMain)

        ivMain.setOnClickListener { dialog.dismiss() }

        dialog.setOnDismissListener {
            callback.onImageFinishedLoaded()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    interface ShowDialogImageCallback {
        fun onImageFinishedLoaded()
    }
}