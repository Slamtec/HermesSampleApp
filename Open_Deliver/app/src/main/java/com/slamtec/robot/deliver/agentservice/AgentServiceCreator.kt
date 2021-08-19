package com.slamtec.robot.deliver.agentservice

import com.slamtec.robot.deliver.RobotConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object AgentServiceCreator {
    private val httpClient = OkHttpClient.Builder().retryOnConnectionFailure(true).connectTimeout(6, TimeUnit.SECONDS).addInterceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder().addHeader("Connection", "close")
        chain.proceed(requestBuilder.build())
    }.build()

    private val retrofit =
        Retrofit.Builder().baseUrl(RobotConfig.AGENT_URL).addConverterFactory(GsonConverterFactory.create()).client(httpClient)
            .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)
}