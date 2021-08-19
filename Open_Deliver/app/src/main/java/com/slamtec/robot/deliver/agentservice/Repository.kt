package com.slamtec.robot.deliver.agentservice

import androidx.lifecycle.liveData
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {



    fun queryPois() = fire(Dispatchers.IO) {
        val pois = RobotNetwork.queryPois()
        if (pois != null) {
            Result.success(pois)
        } else {
            Result.failure(RuntimeException("query pois failed"))
        }

    }


    fun queryDeliveryStage() = loopFire(Dispatchers.IO) {
        val stage = RobotNetwork.queryDeliveryStage()
        Result.success(stage)
    }

    fun queryTasks(status: String?, type: String?) = fire(Dispatchers.IO) {
        val tasks = RobotNetwork.queryTasks(status, type)
        Result.success(tasks)
    }

    fun queryEvents() = loopFire(Dispatchers.IO) {
        val stage = RobotNetwork.queryEvents()
        Result.success(stage)
    }

    fun queryPowerStatus() = loopFire(Dispatchers.IO) {
        val powerStatus = RobotNetwork.queryPowerStatus()
        if (powerStatus.batteryPercentage != null) {
            Result.success(powerStatus)
        } else {
            Result.failure(RuntimeException("query power_status failed"))
        }
    }

    fun queryConfigInfo() = fire(Dispatchers.IO) {
        val configInfoBean = RobotNetwork.queryConfigurations()
        Result.success(configInfoBean)
    }

    fun operateBox(BoxCmd: BoxCmd) = fire(Dispatchers.IO) {
        val response = RobotNetwork.operateBox(BoxCmd)
        Result.success(response)
    }

    fun queryBox(time: Int, requestBox: RequestBox) = loopFireLimit(Dispatchers.IO, time) {
        val box = RobotNetwork.queryBox(requestBox)
        Result.success(box)
    }

    fun queryBoxOperationResult(cargo_id: String, box_id: String, time: Int) =
        loopFireLimit(Dispatchers.IO, time) {
            val operationResult = RobotNetwork.queryBoxOperationResult(cargo_id, box_id)
            Result.success(operationResult)
        }

    fun queryCargos() = fire(Dispatchers.IO) {
        val cargos = RobotNetwork.queryCargos()
        Result.success(cargos)
    }

    fun queryAssignedCargos() = fire(Dispatchers.IO) {
        val assignedCargos = RobotNetwork.queryAssignedCargos()
        Result.success(assignedCargos)
    }

    fun refill(refillCargos: List<RefillCargo>) = fire(Dispatchers.IO) {
        val refillResponse = RobotNetwork.refill(refillCargos)
        Result.success(refillResponse)
    }

    fun enableTaskExecution(enable: TaskExecution) = fire(Dispatchers.IO) {
        val enableResponse = RobotNetwork.enableTaskExecution(enable)
        Result.success(enableResponse)
    }

    fun startPickup() = fire(Dispatchers.IO) {
        val startPickupResponse = RobotNetwork.startPickup()
        Result.success(startPickupResponse)
    }

    fun endPickup() = fire(Dispatchers.IO) {
        val endPickupResponse = RobotNetwork.endPickup()
        Result.success(endPickupResponse)
    }

    fun createTask(order:Order) = fire(Dispatchers.IO) {
        val createOrderResponse = RobotNetwork.createTask(order)
        Result.success(createOrderResponse)
    }


    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure<T>(e)
            }
            emit(result)
        }

    private fun <T> loopFire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            while (true) {
                val result = try {
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure<T>(e)
                }
                emit(result)
                delay(1000)
            }
        }

    private fun <T> loopFireLimit(
        context: CoroutineContext,
        time: Int,
        block: suspend () -> Result<T>
    ) =
        liveData(context) {
            var i = 0
            while (i < time) {
                val result = try {
                    i++
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure<T>(e)
                }
                emit(result)
                delay(1000)
            }
        }

}