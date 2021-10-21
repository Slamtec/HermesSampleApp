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
    @GET("core/system/v1/power/status")
    fun queryPowerStatus(): Call<PowerStatus>

    @GET("core/system/v1/robot/health")
    fun queryRobotHealth(): Call<RobotHealth>

    @GET("multi-floor/map/v1/pois")
    fun queryPois(): Call<ArrayList<Poi>>

    @GET("delivery/v1/admin/password")
    fun queryPassword(): Call<PasswordResponse>

    @POST("core/motion/v1/actions")
    fun createMoveAction(@Body action: CreateMoveAction): Call<ActionResponse>

    @POST("core/system/v1/power/:shutdown")
    fun shutdown(@Body shutdownRequest: ShutdownRequest): Call<Boolean>

    @GET("core/motion/v1/actions/{action_id}")
    fun getActionStatus(@Path("action_id") action_id: Int): Call<ActionResponse>

    @DELETE("core/motion/v1/actions/:current")
    fun stopCurrentAction(): Call<Void>

    @GET("core/system/v1/robot/info")
    fun getDeviceInfo(): Call<DeviceInfo>

    @GET("core/system/v1/parameter")
    fun getSystemParameter(@Query("param") param: String): Call<String>

    @PUT("core/system/v1/parameter")
    fun setSystemParameter(@Body systemParameter: SystemParameter): Call<Boolean>
}