package com.slamtec.robot.deliver.model


class Order(val location: Location, val cargos: List<OrderCargo>)


data class OrderCargo(val cargo_id: String, val boxes: List<String>)

data class OrderResponse(val result: Boolean, val errors: Any?)

data class AssignedCargo(val target:String,val order_id:String,val cargo_id: String, val boxes: List<String>)

enum class MileStone {
    GOING_TO_ELEVATOR, WAIT_FOR_ELEVATOR, GOING_INTO_ELEVATOR, IN_ELEVATOR,
    GOING_OUT_OF_ELEVATOR, ON_DELIVERING, ARRIVED_AT_DELIVERY_POSE, WAIT_REFILL_TIMEOUT,
    USER_OPERATE_ROBOT
}

enum class CargoType {
    TAKEOUT, RETAIL
}

data class DeliveryStageResponse(
    val stage: String,
    val status: String?,
    val milestone: String?,
    val current_floor: String?,
    val target_floor: String?,
    val pickup: PickUp?,
    val info: Any?
)


data class Sku(val sku_id: Int, val sku_name: String, val sku_pic: String)


data class DeliveringInfo(val location: Location, val task: TaskInfo.Task) {
    data class Location(val poi_name: String, val type: String)
}

enum class PoiType {
    ROOM, RECEPTION, REFILL
}

class Location(val poi_name: String)


data class TaskInfo(val id: String, val result: Any, val status: String, val task: Task) {
    data class Task(
        val cargos: List<Cargo>,
        val no_pickup_wait: Boolean,
        val req_id: String,
        val target: String,
        val type: String,
        val order_id: String,
        val failed_tasks: List<FailedTask>
    ) {
        data class Cargo(val cargo_id: String, val boxes: List<String>)
        data class FailedTask(val target: String, val cargos: List<Cargo>)
    }
}

data class PickUp(val num_picked_up: Int, val num_total: Int, val result: List<Result>) {
    data class Result(
        val cargo_id: String,
        val box_id: String,
        val result: String,
        val reason: String
    )
}

enum class ResultType {
    SUCCEEDED, FAILED
}

enum class ReasonType {
    BOX_NOT_FOUND, BOX_UNDER_ERROR, FAILED_TO_OPEN_DOOR, NO_PICKUP, CLOSE_DOOR_TIMEOUT
}

class Poi(
    val building: String,
    val floor: String,
    val id: String,
    val poi_name: String,
    val pose: Pose,
    val type: String
)

class Pose(val x: Double, val y: Double, val yaw: Double)
class PowerStatus(
    val batteryPercentage: Int,
    val dockingStatus: String,
    val isCharging: Boolean,
    val isDCConnected: Boolean,
    val powerStage: String,
    val sleepMode: String
)

data class ConfigInfoBean(
    val cargos: List<Cargo>,
    val cloud_version: String,
    val device_sn: String,
    val firmware_version: String,
    val enable_stock_detection: Boolean
)


enum class OperateType(val cmd: String) {
    OPEN("open"), CLOSE("close")
}



enum class BoxDoorStatus {
    OPEN, CLOSED, OPENING, CLOSING, ERROR, DONE, SEMIOPEN
}


data class TaskExecution(val enable_task_execution: Boolean)

enum class TaskType(val type: String) {
    RETAIL("retail"), TAKEOUT("takeout"), REFILL("refill"), COLLECT("collect")
}

enum class TaskStatus(val status: String) {
    READY("ready"), FAILED("failed")
}

data class BoxCmd(val cargo_id: String, val box_id: String, val operateType: OperateType)


enum class StockStatus {
    EMPTY, SEMIFULL, FULL
}

enum class Status {
    EMPTY, NOT_EMPTY, ERROR, FULL
}


enum class EventType(val type: Int) {
    ARRIVE_AT_RECEPTION(1),
    ON_DELIVERING(2),
    PATH_OCCUPIED(3),
    ENTER_ELEVATOR_TIMEOUT(4),
    IN_ELEVATOR(5),
    GOING_OUT_ELEVATOR(6),
    BOX_OPENED(7),
    GOODBYE(8),
    BE_KIDNAPPED(9),
    ON_DOCK(10),
    START_DELIVERING(11),
    TURNING_ROUND(12),
    OPEN_MULTIPLE_BOXES(13),
    START_FROM_DOCK(14),
    WAIT_ELEVATOR(15),
    ARRIVE_AT_START_FLOOR(16),
    ENTER_ELEVATOR(17),
    ENTER_ELEVATOR_OCCUPIED(18),
    TURNING_ROUND_IN_ELEVATOR(19),
    ARRIVE_AT_TARGET_FLOOR(20),
    LEAVE_ELEVATOR_OCCUPIED(21),
    LEAVE_ELEVATOR_FAILED(22),
    FIRST_ORDER_FOR_SIX_SIMPLE(23),
    FIRST_ORDER_FOR_SIX_MULTIPLE(24),
    BACK_TO_RECEPTION_FOR_FAILED_ORDER(25),
    PASS_THE_NARROW_CORRIDOR(26)
}

data class Event(val id: Int, val type: String)


enum class LockStatus {
    LOCKED, UNLOCKED
}


data class Cargo(
    val boxes: List<Box>,
    val id: String,
    val orientation: String,
    val pos: Int,
    val layer: Int,
    val type: String
) {

    data class Box(
        var id: String,
        var door_status: String = BoxDoorStatus.CLOSED.toString(),
        var status: String = Status.FULL.toString(),
        var stock_status: String = StockStatus.FULL.toString(),
        var lock_status: String = LockStatus.LOCKED.toString(),
        val errors: List<String>? = null
    )
}


data class BoxOperationResult(
    val box: Cargo.Box,
    val type: String,
    val cargo_id: String,
    val stage: String,
    val reason: String
) {
    enum class OperateStage {
        DONE, IN_PROGRESS, FAILED
    }

    enum class Reason {
        CLOSE_DOOR_STALLED
    }
}

data class FailedBox(val reason: String?, val cargo_id: String?, val box: Cargo.Box)


data class CargoBox(val cargo_id: String, var box: Cargo.Box)

data class FailedTaskBox(val target: String, val cargo_id: String, var box: Cargo.Box)


data class RequestBox(val cargo_id: String, val box_id: String)


data class RefillCargo(val cargo_id: String, val boxes: List<RefillBox>) {
    data class RefillBox(val box_id: String, val sku_id: Int, val count: Int)
}


data class LockResponse(val result: Boolean, val cargos: List<LockCargo>?) {
    data class LockCargo(val cargo_id: String, val boxes: List<Box>) {
        data class Box(
            val box_id: String,
            var count: Int,
            val sku_id: Int,
            val sku_name: String,
            val sku_pic: String
        )
    }
}


data class DeviceError(val error: List<Error>, val failed_tasks: List<Task>) {
    data class Error(
        val error_code: Int,
        val message: String,
        val error_level: Int,
        val component: Int
    )

    data class Task(
        val id: String,
        val type: String,
        val target: String,
        val cargos: List<Cargo>
    ) {
        data class Cargo(val cargo_id: String, val boxes: List<String>)
    }
}

data class Qrcode(val url: String)
data class DeliveryResource(
    val qr_code: Qrcode,
    val ui_resources: ConfigResource,
    val delivery_waiting_time_in_secs: Int,
    val refill_timeout_settings: TimeoutSettings
)

data class TimeoutSettings(
    val notify_no_action_in_secs: Int,
    val task_failure_no_action_in_secs: Int
)

data class ConfigResource(
    val play_voice_node_list: List<VoiceNode>?,
    val robot_name: String?,
    val volume_config_list: List<VolumeConfig>?,
    val timeout_settings: TimeoutSettings,
    val low_battery_level: LowBatteryLevel,
    val voice_sn: String?
) {
    data class TimeoutSettings(
        val T0: Int,
        val T1: Int,
        val T2: Int,
        val T3: Int,
        val T4: Int,
        val T5: Int,
        val T6: Int,
        val T7: Int,
        val T8: Int,
        val T9: Int,
        val T10: Int,
        val T11: Int,
        val T12: Int,
        val T13: Int,
        val T14: Int,
        val T15: Int,
        val T16: Int
    )

    data class LowBatteryLevel(
        val level1: Int,
        val level2: Int,
        val level3: Int,
        val level4: Int
    )
}

data class VoiceNode(
    val content: List<String>,
    val interval_count: Int,
    val play_type: Int,
    val repeat_count: Int
)



data class VolumeConfig(val begin_time_str: String, val end_time_str: String, val volume: Int)


enum class BoxState {
    OPEN, CLOSED, OPENING, CLOSING, ERROR, DONE, SEMIOPEN
}

