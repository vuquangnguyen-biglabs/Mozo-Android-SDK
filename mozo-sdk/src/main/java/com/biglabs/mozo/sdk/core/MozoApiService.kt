package com.biglabs.mozo.sdk.core

import android.content.Context
import com.biglabs.mozo.sdk.BuildConfig
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MozoApiService {

    companion object {

        private const val BASE_URL = "http://18.136.55.245:8080/solomon/api/"

        @Volatile
        private var instance: MozoApiService? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            if (instance == null) instance = createService(context)
            instance
        }!!

        private fun createService(context: Context): MozoApiService {
            val client = OkHttpClient.Builder().addInterceptor {
                val accessToken = AuthStateManager.getInstance(context).current.accessToken
                val original = it.request()
                val request = original.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build()
                it.proceed(request)
            }
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                client.addInterceptor(logging)
            }

            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client.build())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MozoApiService::class.java)
        }
    }

    @GET("contacts")
    fun getContacts(): Deferred<Response<List<Models.Contact>>>

    @POST("contacts")
    fun saveContact(@Body contact: Models.Contact): Deferred<Response<Models.Contact>>

    @GET("user-profile")
    fun fetchProfile(): Deferred<Response<Models.Profile>>

    @PUT("user-profile/exchange-info")
    fun saveExchangeInfo(exchangeInfo: Models.Profile): Deferred<Response<Models.Profile>>

    @PUT("user-profile/settings")
    fun saveSettings(notificationThreshold: Int = 0): Deferred<Response<Models.Profile>>

    @PUT("user-profile/wallet")
    fun saveWallet(@Body walletInfo: Models.WalletInfo): Deferred<Response<Models.Profile>>

    @GET("solo/contract/solo-token/balance/{address}")
    fun getBalance(@Path("address") address: String): Deferred<Response<Models.BalanceInfo>>

    @POST("solo/contract/solo-token/transfer")
    fun createTransaction(@Body request: Models.TransactionRequest): Deferred<Response<Models.TransactionResponse>>

    @POST("solo/contract/solo-token/send-signed-tx")
    fun sendTransaction(@Body request: Models.TransactionResponse): Deferred<Response<Models.TransactionResponse>>

    @GET("solo/contract/solo-token/txhistory/{address}")
    fun getTransactionHistory(
            @Path("address") address: String,
            @Query("page") page: Int,
            @Query("size") size: Int
    ): Deferred<Response<List<Models.TransactionHistory>>>
}