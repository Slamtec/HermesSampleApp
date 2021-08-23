package com.slamtec.fooddelivery.model

/**
 * File   : RobotBean
 * Author : Qikun.Xiong
 * Date   : 2021/8/10 5:19 PM
 */
data class PowerStatus(
        val batteryPercentage: Int,
        val dockingStatus: String,
        val isCharging: Boolean,
        val isDCConnected: Boolean,
        val powerStage: String,
        val sleepMode: String
)

data class Poi(
        val id: String,
        val poi_name: String,
        val type: String,
        val floor: String,
        val building: String,
        val pose: Pose
)

/**
 * status: 0-待放入菜品,1-选择餐桌号,2-已选择餐桌号，待取餐,3-到达餐桌,4-已取餐,5-未取餐，取餐超时
 */
data class PlateInfo(
        var plateName: String = "1L",
        var status: Int = 0,
        var selectedTable: String = "null"
)

data class Pose(val x: Double, val y: Double, val yaw: Double)
data class PasswordResponse(val delivery_admin_password: PasswordBean) {
    data class PasswordBean(val password: String, val expires: String)
}

data class CreateMoveAction(
        val action_name: String,
        val options: Options?
) {
    data class Options(val target: MoveTarget, val move_options: MoveOptions) {
        data class MoveTarget(val poi_name: String?)
        data class MoveOptions(val flags: List<String>, val fail_retry_count: Int)
    }
}

/**
 * status：0-newborn，1-working，2-paused，3-done
 */
data class ActionResponse(
        val action_id: Int,
        val action_name: String,
        val stage: String,
        val state: ActionState
) {
    data class ActionState(val status: Int, val result: Int, val reason: String)
}

data class ShutdownRequest(val shutdown_time_interval: Int, val restart_time_interval: Int)

/**
 * component: 0 User, 1 System, 2 Power, 3 Motion, 4 Sensor, 255 Unknown
 * level:0 Healthy, 1 Warn, 2 Error, 4 Fatal, 255 Unknown
 */
data class RobotHealth(val hasWarning: Boolean, val hasError: Boolean, val hasFatal: Boolean, val baseError: List<BaseError>) {
    data class BaseError(val id: Int, val component: Int, val errorCode: Int, val level: Int, val message: String)
}

data class DeviceInfo(val manufacturerId: Int, val manufacturerName: String, val modelId: Int, val modelName: String, val deviceID: String, val hardwareVersion: String, val softwareVersion: String)

data class SystemParameter(val param: String, val value: String)

data class DateViewBean(val time: String, val day: String, val week: String)

enum class ViewType {
    HOME, DELIVERING, INPUT_TABLE, ARRIVED_TABLE, GOTO_GET_FOOD, SETTING, GO_HOME, ERROR
}

enum class PoiType {
    ROOM, RECEPTION, REFILL
}