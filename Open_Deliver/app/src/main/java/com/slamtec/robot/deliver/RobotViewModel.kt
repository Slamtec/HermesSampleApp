package com.slamtec.robot.deliver

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slamtec.robot.deliver.agentservice.Repository
import com.slamtec.robot.deliver.model.*

class RobotViewModel : ViewModel() {
    val viewType = MutableLiveData<RobotViewType>()


    var configurations: ConfigInfoBean? = null

    var openBox: CargoBox? = null

    var idleBoxes = arrayListOf<CargoBox>()

    var takeoutBoxes = arrayListOf<CargoBox>()

    var failedTakeoutBoxes = arrayListOf<FailedTaskBox>()

    var assignedTakeoutBoxes = arrayListOf<CargoBox>()

    var failedTakeoutTasks = arrayListOf<TaskInfo>()

    var requestOrderList = arrayListOf<Order>()

    private var preViewType: RobotViewType? = null

    var mRoomNumberLength = 4

    var mRoomList = arrayListOf<String>()

    var inputNumber: String? = null

    var taskInfo: TaskInfo? = null

    var deviceError: DeviceError? = null

    var resource: DeliveryResource? = null

    var deliveryStageResponse = MutableLiveData<DeliveryStageResponse>()

    val deliveryStage = MutableLiveData<String>()

    var deliveringInfo: DeliveringInfo? = null

    var orderUnFinishCountDown = 0L

    var isTimeoutCheck = true

    var isCreateNextOrder = false

    var powerStatus: PowerStatus? = null

    fun queryPois() = Repository.queryPois()

    fun queryConfigInfo() = Repository.queryConfigInfo()

    fun queryDeliveryStage() = Repository.queryDeliveryStage()

    fun queryPowerStatus() = Repository.queryPowerStatus()

    fun operateBox(BoxCmd: BoxCmd) = Repository.operateBox(BoxCmd)

    fun queryBox(queryLimit: Int, requestBox: RequestBox) =
        Repository.queryBox(queryLimit, requestBox)

    fun queryBoxOperationResult(cargo_id: String, box_id: String, queryLimit: Int) =
        Repository.queryBoxOperationResult(cargo_id, box_id, queryLimit)

    fun queryCargos() = Repository.queryCargos()

    fun queryAssignedCargos() = Repository.queryAssignedCargos()

    fun queryTasks(status: String? = null, type: String? = null) =
        Repository.queryTasks(status, type)

    fun enableTaskExecution(enable: TaskExecution) = Repository.enableTaskExecution(enable)

    fun startPickup() = Repository.startPickup()

    fun endPickup() = Repository.endPickup()

    fun createTask(order:Order) = Repository.createTask(order)

    fun queryEvents() = Repository.queryEvents()



    fun updateViewType(type: RobotViewType) {
        if (viewType.value != type) {
            preViewType = viewType.value
            viewType.value = type
        }
    }

    fun backView() {
        viewType.value = preViewType
    }

}