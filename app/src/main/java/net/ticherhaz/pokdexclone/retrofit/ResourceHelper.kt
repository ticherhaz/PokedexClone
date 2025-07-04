package my.resitmudah.retrofit

import android.content.Context
import net.ticherhaz.pokdexclone.retrofit.Resource
import net.ticherhaz.pokdexclone.utils.Tools

object ResourceHelper {
    fun <T> handleResourceError(context: Context, errorMessage: Resource.Error<T>) {
        val messageInt = errorMessage.messageInt
        val message = errorMessage.message
        val errorReader = errorMessage.errorReader
        if (messageInt != null) {
            Tools.showToast(context, context.getString(messageInt))
        }
        if (message != null) {
            Tools.showToast(context, message)
        }
        if (errorReader != null) {
            //Do debug
        }
    }
}