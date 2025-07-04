package net.ticherhaz.pokdexclone.retrofit

import net.ticherhaz.pokdexclone.enumerator.HttpCode
import retrofit2.Response

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val messageInt: Int? = null,
    val code: Int? = null,
    val errorReader: String? = null,
) {
    class Initialize<T> : Resource<T>()
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(
        message: String? = null,
        messageInt: Int? = null,
        data: T? = null,
        code: Int? = null,
        errorReader: String? = null
    ) :
        Resource<T>(data, message, messageInt, code, errorReader)

    companion object {
        suspend fun <T> getResponse(call: suspend () -> Response<T>): Resource<T> {
            val response = call.invoke()
            when {
                response.isSuccessful -> {
                    response.body().let { resultResponse ->
                        return Success(resultResponse)
                    }
                }

                response.code() == HttpCode.UNAUTHORIZED.code -> {
                    return Error(
                        messageInt = HttpCode.UNAUTHORIZED.messageInt,
                        code = HttpCode.UNAUTHORIZED.code,
                        errorReader = response.errorBody()?.byteStream()?.bufferedReader()
                            .use { it?.readText() }
                    )
                }

                response.code() == HttpCode.NOT_FOUND.code -> {
                    return Error(
                        messageInt = HttpCode.NOT_FOUND.messageInt,
                        code = HttpCode.NOT_FOUND.code,
                        errorReader = response.errorBody()?.byteStream()?.bufferedReader()
                            .use { it?.readText() }
                    )
                }

                response.code() == HttpCode.SERVER_ERROR.code -> {
                    return Error(
                        messageInt = HttpCode.SERVER_ERROR.messageInt,
                        code = HttpCode.SERVER_ERROR.code,
                        errorReader = response.errorBody()?.byteStream()?.bufferedReader()
                            .use { it?.readText() }
                    )
                }

                response.code() == HttpCode.RETRY_TO_CONNECT.code -> {
                    return Error(
                        messageInt = HttpCode.RETRY_TO_CONNECT.messageInt,
                        code = HttpCode.RETRY_TO_CONNECT.code,
                        errorReader = response.errorBody()?.byteStream()?.bufferedReader()
                            .use { it?.readText() }
                    )
                }

                else -> {
                    return Error(
                        messageInt = HttpCode.UNKNOWN_ERROR.messageInt,
                        code = HttpCode.UNKNOWN_ERROR.code,
                        errorReader = response.errorBody()?.byteStream()?.bufferedReader()
                            .use { it?.readText() }
                    )
                }
            }
        }
    }
}