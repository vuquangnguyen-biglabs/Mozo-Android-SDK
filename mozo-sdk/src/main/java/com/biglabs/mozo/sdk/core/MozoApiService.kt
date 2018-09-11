package com.biglabs.mozo.sdk.core

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PUT

interface MozoApiService {

    companion object {

        private const val BASE_URL = "http://18.136.55.245:8080/solomon/api"

        fun create(): MozoApiService {
            return Retrofit.Builder()
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()
                    .create(MozoApiService::class.java)
        }
    }

    /* User Profile */
    @GET("/user-profile")
    fun fetchProfile(userId: String): Deferred<Response<Models.Profile>>

    @PUT("/user-profile/exchange-info")
    fun saveExchangeInfo(exchangeInfo: Models.Profile): Deferred<Response<Models.Profile>>

    @PUT("/user-profile/settings")
    fun saveSettings(notificationThreshold: Int = 0): Deferred<Response<Models.Profile>>

    @PUT("/user-profile/wallet")
    fun saveWallet(encryptedSeed: String): Deferred<Response<Models.Profile>>
}