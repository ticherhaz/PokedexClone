package net.ticherhaz.pokdexclone.retrofit

import net.ticherhaz.pokdexclone.utils.ConstantApi
import net.ticherhaz.pokdexclone.utils.QuickSave
import net.ticherhaz.pokdexclone.utils.Tools
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofitClient: Retrofit.Builder by lazy {

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()

        val decryptedApiUrl = QuickSave.getInstance().fixedDecrypt(ConstantApi.API_URL)
        Tools.logSimple("Decrypted API URL: $decryptedApiUrl")

        Retrofit.Builder()
            .baseUrl(decryptedApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
    }

    val apiInterface: ApiInterface by lazy {
        retrofitClient
            .build()
            .create(ApiInterface::class.java)
    }

    private fun loggingInterceptor(): Interceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        return loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    }
}