package com.slamtec.robot.deliver.agentservice

import com.slamtec.robot.deliver.model.*
import retrofit2.Call
import retrofit2.http.*

interface AgentApi {
    @GET("api/delivery/v1/tasks/stage")
    fun queryDeliveryStage(): Call<DeliveryStageResponse>

    @GET("api/multi-floor/map/v1/pois")
    fun queryPois(): Call<ArrayList<Poi>>

    @GET("api/delivery/v1/configurations")
    fun queryConfigurations(): Call<ConfigInfoBean>

    @GET("api/core/system/v1/power/status")
    fun queryPowerStatus(): Call<PowerStatus>

    @PUT("/api/delivery/v1/cargos/{cargo_id}/boxes/{box_id}/:{cmd}")
    fun operateBox(
        @Path("cargo_id") cargo_id: String,
        @Path("box_id") box_id: String,
        @Path("cmd") cmd: String
    ): Call<Void>

    @GET("/api/delivery/v1/cargos/{cargo_id}/boxes/{box_id}")
    fun queryBox(
        @Path("cargo_id") cargo_id: String,
        @Path("box_id") box_id: String
    ): Call<Cargo.Box>

    @GET("api/delivery/v1/cargos/{cargo_id}/boxes/{box_id}/operation_result")
    fun queryBoxOperationResult(
        @Path("cargo_id") cargo_id: String,
        @Path("box_id") box_id: String
    ): Call<BoxOperationResult>


    @GET("/api/delivery/v1/cargos")
    fun queryCargos(): Call<List<Cargo>>

    @GET("/api/delivery/v1/cargos/assigned")
    fun queryAssignedCargos(): Call<List<AssignedCargo>>

    @POST("/api/delivery/v1/tasks")
    fun createTask(@Body order: Order): Call<OrderResponse>

    @GET("/api/delivery/v1/tasks?status&type")
    fun queryTasks(
        @Query("status") status: String?,
        @Query("type") type: String?
    ): Call<List<TaskInfo>>

    @PUT("/slamware/deliveries/v2/refill/:{lock_state}")
    fun lockStock(@Path("lock_state") lock_state: String): Call<LockResponse>

    @GET("/slamware/deliveries/v2/refill/skus")
    fun querySkus(): Call<List<Sku>>

    @PUT("/slamware/deliveries/v2/refill")
    fun refill(@Body refillCargos: List<RefillCargo>): Call<Void>

    @PUT("/api/delivery/v1/tasks/:task_execution")
    fun enableTaskExecution(@Body enable_task_execution: TaskExecution): Call<Void>

    @PUT("api/delivery/v1/tasks/:start_pickup")
    fun startPickup(): Call<Void>

    @PUT("api/delivery/v1/tasks/:end_pickup")
    fun endPickup(): Call<Void>

    @GET("/api/delivery/v1/events")
    fun queryEvents(): Call<List<Event>>
}