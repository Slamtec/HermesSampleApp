package com.slamtec.fooddelivery.api

import androidx.lifecycle.liveData
import com.slamtec.fooddelivery.model.CreateMoveAction
import com.slamtec.fooddelivery.model.ShutdownRequest
import com.slamtec.fooddelivery.model.SystemParameter
import com.slamtec.fooddelivery.utils.LogMgr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

/**
 * File   : Repository
 * Author : Qikun.Xiong
 * Date   : 2021/8/6 6:29 PM
 */
object Repository {

    fun queryPowerStatus() = loopFire(Dispatchers.IO) {
        val powerStatus = RobotNetwork.queryPowerStatus()
        Result.success(powerStatus)
    }

    fun queryRobotHealth() = loopFire(Dispatchers.IO) {
        val rp = RobotNetwork.queryRobotHealth()
        Result.success(rp)
    }

    fun queryPois() = fire(Dispatchers.IO) {
        val pois = RobotNetwork.queryPois()
        Result.success(pois)
    }

    fun getDeviceInfo() = fire(Dispatchers.IO) {
        val rp = RobotNetwork.getDeviceInfo()
        Result.success(rp)
    }

    fun createNewAction(action: CreateMoveAction) = fire(Dispatchers.IO) {
        val actionResponse = RobotNetwork.createNewAction(action)
        Result.success(actionResponse)
    }

    fun getActionStatus(actionId: Int) = loopFireFlexible(Dispatchers.IO, 1000) {
        val ar = RobotNetwork.queryActionStatus(actionId)
        Result.success(ar)
    }

    fun getSystemParameter(param: String) = fire(Dispatchers.IO) {
        Result.success(RobotNetwork.getSystemParameter(param))
    }

    fun setSystemParameter(param: SystemParameter) = fire(Dispatchers.IO) {
        Result.success(RobotNetwork.setSystemParameter(param))
    }

    fun queryPassword() = fire(Dispatchers.IO) {
        val passResponse = RobotNetwork.queryPassword()
        LogMgr.i(passResponse.toString())
        if (passResponse.delivery_admin_password != null) {
            Result.success(passResponse.delivery_admin_password)
        } else {
            Result.failure(RuntimeException("query password failed"))
        }
    }

    fun shutdown(shutdownRequest: ShutdownRequest) = fire(Dispatchers.IO) {
        val shutdownResponse = RobotNetwork.shutdown(shutdownRequest)
        Result.success(shutdownResponse)
    }

    fun stopCurrentAction() = fire(Dispatchers.IO) {
        val rp = RobotNetwork.stopCurrentAction()
        Result.success(rp)
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
            liveData(context) {
                val result = try {
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogMgr.e("fire exception:$e")
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
                        LogMgr.e("loopFire exception:$e")
                        Result.failure<T>(e)
                    }
                    emit(result)
                    delay(1000)
                }
            }

    private fun <T> loopFireFlexible(context: CoroutineContext, time: Long, block: suspend () -> Result<T>) =
            liveData(context) {
                while (true) {
                    val result = try {
                        block()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        LogMgr.e("loopFireFlexible exception:${e.printStackTrace()}")
                        Result.failure<T>(e)
                    }
                    emit(result)
                    delay(time)
                }
            }

    private fun <T> loopFireLimit(context: CoroutineContext, time: Int, block: suspend () -> Result<T>) =
            liveData(context) {
                var i = 0
                while (i < time) {
                    val result = try {
                        i++
                        block()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        LogMgr.e("loopFireLimit exception:$e")
                        Result.failure<T>(e)
                    }
                    emit(result)
                    delay(1000)
                }
            }
}