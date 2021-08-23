package com.slamtec.fooddelivery.api

import com.slamtec.fooddelivery.model.CreateMoveAction
import com.slamtec.fooddelivery.model.ShutdownRequest
import com.slamtec.fooddelivery.model.SystemParameter
import retrofit2.Response
import retrofit2.await

/**
 * File   : RobotNetwork
 * Author : Qikun.Xiong
 * Date   : 2021/8/6 6:29 PM
 */
object RobotNetwork {
    private val localService = AgentServiceCreator.create(AgentApi::class.java, 1L)
    suspend fun queryPowerStatus() = localService.queryPowerStatus().await()
    suspend fun queryRobotHealth() = localService.queryRobotHealth().await()
    suspend fun queryPois() = localService.queryPois().await()
    suspend fun queryPassword() = localService.queryPassword().await()
    fun shutdown(shutdownRequest: ShutdownRequest): Response<Boolean> =
            localService.shutdown(shutdownRequest).execute()

    suspend fun createNewAction(action: CreateMoveAction) = localService.createMoveAction(action).await()
    suspend fun queryActionStatus(actionId: Int) = localService.getActionStatus(actionId).await()
    suspend fun stopCurrentAction() = localService.stopCurrentAction().await()
    suspend fun getDeviceInfo() = localService.getDeviceInfo().await()
    suspend fun getSystemParameter(param: String) = localService.getSystemParameter(param).await()
    suspend fun setSystemParameter(param: SystemParameter) = localService.setSystemParameter(param).await()
}
