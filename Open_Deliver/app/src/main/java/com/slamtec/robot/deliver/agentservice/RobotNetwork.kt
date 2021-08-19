package com.slamtec.robot.deliver.agentservice

import com.slamtec.robot.deliver.model.*
import retrofit2.Response
import retrofit2.await

object RobotNetwork {
    private val robotService = AgentServiceCreator.create(AgentApi::class.java)

    suspend fun queryPowerStatus() = robotService.queryPowerStatus().await()
    suspend fun queryConfigurations() = robotService.queryConfigurations().await()


    suspend fun queryDeliveryStage() = robotService.queryDeliveryStage().await()
    suspend fun queryPois() = robotService.queryPois().await()

    fun operateBox(boxCmd: BoxCmd): Response<Void> =
        robotService.operateBox(boxCmd.cargo_id, boxCmd.box_id, boxCmd.operateType.cmd).execute()

    suspend fun queryBox(requestBox: RequestBox) =
        robotService.queryBox(requestBox.cargo_id, requestBox.box_id).await()

    suspend fun queryBoxOperationResult(cargo_id: String, box_id: String) =
        robotService.queryBoxOperationResult(cargo_id, box_id).await()

    suspend fun queryCargos() = robotService.queryCargos().await()

    suspend fun queryTasks(status: String?, type: String?): List<TaskInfo> {
        return robotService.queryTasks(status, type).await()
    }

    suspend fun queryAssignedCargos() = robotService.queryAssignedCargos().await()

    fun createTask(order:Order): Response<OrderResponse> =
        robotService.createTask(order).execute()

    fun refill(refillCargos: List<RefillCargo>): Response<Void> =
        robotService.refill(refillCargos).execute()

    fun enableTaskExecution(enable: TaskExecution): Response<Void> =
        robotService.enableTaskExecution(enable).execute()

    fun startPickup(): Response<Void> = robotService.startPickup().execute()

    fun endPickup(): Response<Void> = robotService.endPickup().execute()


    suspend fun queryEvents() =
        robotService.queryEvents().await()
}