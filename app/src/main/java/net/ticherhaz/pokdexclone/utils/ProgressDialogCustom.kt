package net.ticherhaz.pokdexclone.utils

import android.app.Dialog
import android.content.Context
import android.view.View
import net.ticherhaz.pokdexclone.R
import net.ticherhaz.pokdexclone.utils.ProgressDialogCustom.hide

object ProgressDialogCustom {

    private var dialog: Dialog? = null

    /**
     * Shows a progress dialog with customizable cancelable behavior.
     * Once implemented, ensure to call [hide] in the host Activity's onDestroy()
     * to prevent potential leaks.
     *
     * @param context The context used to create the dialog (should be Activity context)
     * @param cancelable Whether the dialog can be canceled by tapping outside (default: false)
     */
    fun show(context: Context, cancelable: Boolean = false) {
        if (dialog != null) {
            dialog = null
        }

        dialog = Dialog(context, R.style.TransparentProgressDialog).apply {
            setContentView(View.inflate(context, R.layout.progress_dialog_custom, null))
            setCancelable(cancelable)
            show()
        }
    }

    /**
     * Dismisses the currently showing progress dialog if present
     */
    fun hide() {
        dialog?.dismiss()
        dialog = null
    }
}