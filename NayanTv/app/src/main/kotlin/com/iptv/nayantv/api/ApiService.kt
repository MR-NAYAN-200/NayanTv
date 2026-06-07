package com.iptv.nayantv.api

import com.iptv.nayantv.model.ChannelResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {

    @GET("api/channels")
    suspend fun getChannels(): Response<ChannelResponse>

    companion object {
        private const val BASE_URL = "https://live-stream-api--systemfuck.replit.app/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
