package com.biglabs.mozo.sdk.core

import com.biglabs.mozo.sdk.MozoSDK
import com.biglabs.mozo.sdk.utils.AuthStateManager
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT


interface MozoApiService {

    companion object {

        private const val BASE_URL = "http://18.136.55.245:8080/solomon/api/"

        fun create(): MozoApiService {
            val accessToken = AuthStateManager.getInstance(MozoSDK.context!!).current.accessToken
            return Retrofit.Builder()
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(
                            OkHttpClient.Builder().addInterceptor {
                                val original = it.request()
                                val request = original.newBuilder()
                                        .header("Authorization", "Bearer $accessToken")
                                        .header("Content-Type", "application/problem+json")
                                        .method(original.method(), original.body())
                                        .build()

                                it.proceed(request)
                            }.build()
                    )
                    .baseUrl(BASE_URL)
                    .build()
                    .create(MozoApiService::class.java)
        }
    }

    /* User Profile */
    @GET("user-profile")
    fun fetchProfile(): Deferred<Response<Models.Profile>>

    @PUT("user-profile/exchange-info")
    fun saveExchangeInfo(exchangeInfo: Models.Profile): Deferred<Response<Models.Profile>>

    @PUT("user-profile/settings")
    fun saveSettings(notificationThreshold: Int = 0): Deferred<Response<Models.Profile>>

    @PUT("user-profile/wallet")
    fun saveWallet(@Body walletInfo: Models.WalletInfo): Deferred<Response<Models.Profile>>
}