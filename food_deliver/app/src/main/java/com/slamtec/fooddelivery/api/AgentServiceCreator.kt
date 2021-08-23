package com.slamtec.fooddelivery.api

import com.slamtec.fooddelivery.constants.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AgentServiceCreator {

    fun <T> create(serviceClass: Class<T>, timeout: Long): T =
            Retrofit.Builder().baseUrl(Constants.AGENT_URL)
                    .addConverterFactory(GsonConverterFactory.create()).client(
                            OkHttpClient.Builder().retryOnConnectionFailure(true)
                                    .connectTimeout(timeout, TimeUnit.SECONDS)
                                    .addInterceptor { chain ->
                                        val originalRequest = chain.request()
                                        val requestBuilder =
                                                originalRequest.newBuilder().addHeader("Connection", "close")
                                        chain.proceed(requestBuilder.build())
                                    }.build()
                    )
                    .build().create(serviceClass)

}