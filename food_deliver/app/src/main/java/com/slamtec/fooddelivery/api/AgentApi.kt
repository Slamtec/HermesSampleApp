package com.slamtec.fooddelivery.api

import com.slamtec.fooddelivery.model.*
import retrofit2.Call
import retrofit2.http.*

/**
 * File   : AgentApi
 * Author : Qikun.Xiong
 * Date   : 2021/8/6 6:28 PM
 */
interface AgentApi {
    @GET("core/systemresource/v1/powerstatus")
    fun queryPowerStatus(): Call<PowerStatus>

    @GET("core/systemresource/v1/robothealth")
    fun queryRobotHealth(): Call<RobotHealth>

    @GET("multi-floor/map/v1/pois")
    fun queryPois(): Call<ArrayList<Poi>>

    @GET("delivery/v1/password")
    fun queryPassword(): Call<PasswordResponse>

    @POST("core/motion/v1/actions")
    fun createMoveAction(@Body action: CreateMoveAction): Call<ActionResponse>

    @POST("core/systemresource/v1/shutdown")
    fun shutdown(@Body shutdownRequest: ShutdownRequest): Call<Boolean>

    @GET("core/motion/v1/actions/{action_id}")
    fun getActionStatus(@Path("action_id") action_id: Int): Call<ActionResponse>

    @DELETE("core/motion/v1/actions/:current")
    fun stopCurrentAction(): Call<Void>

    @GET("core/systemresource/v1/deviceinfo")
    fun getDeviceInfo(): Call<DeviceInfo>

    @GET("core/systemresource/v1/systemparameter")
    fun getSystemParameter(@Query("param") param: String): Call<String>

    @PUT("core/systemresource/v1/systemparameter")
    fun setSystemParameter(@Body systemParameter: SystemParameter): Call<Boolean>
}