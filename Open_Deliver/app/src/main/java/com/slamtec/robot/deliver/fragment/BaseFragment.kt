package com.slamtec.robot.deliver.fragment


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.slamtec.robot.deliver.*
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr

abstract class BaseFragment : Fragment() {

    lateinit var mRobotViewModel: RobotViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    open fun initView() {
        mRobotViewModel = (activity!! as MainActivity).getRobotViewModel()
    }

    fun updateViewType(type: RobotViewType) {
        mRobotViewModel.updateViewType(type)
    }

    fun fullScreenImmersive(view: View) {
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        view?.systemUiVisibility = uiOptions
    }


    fun operateBox(boxCmd: BoxCmd, operateBoxListener: OperateBoxListener? = null) {
        mRobotViewModel.operateBox(boxCmd).observe(this, Observer { operateResult ->
            val operateResponse = operateResult.getOrNull()
            LogMgr.i("${boxCmd.operateType.cmd} response $operateResponse")
            operateResponse?.let {
                if (operateResponse.code() == 200) {
                    LogMgr.i("start ${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id}<<<")
                    queryBoxOperateResultLimit(
                        boxCmd.cargo_id,
                        boxCmd.box_id,
                        25,
                        operateBoxListener
                    )
                } else {
                    LogMgr.e("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} failed")
                    operateBoxListener?.onFailed(null)
                }
            } ?: let {
                LogMgr.e("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} failed")
                operateBoxListener?.onFailed(null)
            }
        })
    }

    private fun queryBoxOperateResultLimit(
        cargo_id: String, box_id: String,
        limit: Int,
        operateBoxListener: OperateBoxListener?
    ) {
        if (activity != null) {
            var limitTime = limit
            mRobotViewModel.queryBoxOperationResult(cargo_id, box_id, limit)
                .observe(this, Observer {
                    val result = it.getOrNull()
                    LogMgr.i("query operate result $result")
                    limitTime--
                    if (limitTime == 0) {
                        LogMgr.i("query operate result finished")
                        operateBoxListener?.onFailed(null)
                    } else if (limitTime > 0) {
                        val stage = result?.stage
                        stage?.let {
                            when (BoxOperationResult.OperateStage.valueOf(stage)) {
                                BoxOperationResult.OperateStage.DONE -> {
                                    limitTime = 0
                                    LogMgr.i("cargo ${result.cargo_id} box ${result.box.id} operate ${result.type} success")
                                    operateBoxListener?.onSuccess(result.box)
                                }
                                BoxOperationResult.OperateStage.IN_PROGRESS -> {

                                }
                                BoxOperationResult.OperateStage.FAILED -> {
                                    limitTime = 0
                                    LogMgr.e("cargo ${result.cargo_id} box ${result.box.id} operate ${result.type} failed")
                                    operateBoxListener?.onFailed(
                                        FailedBox(
                                            result.reason,
                                            result.cargo_id,
                                            result.box
                                        )
                                    )
                                }
                            }
                        } ?: let {
                            LogMgr.e("query operate result stage is null")
                            operateBoxListener?.onFailed(null)
                        }
                    }
                })
        }
    }


    fun lockBox(boxCmd: BoxCmd, operateBoxListener: OperateBoxListener) {
        mRobotViewModel.queryBox(1, RequestBox(boxCmd.cargo_id, boxCmd.box_id))
            .observe(this, Observer {
                val box = it.getOrNull()
                LogMgr.i("$boxCmd result $box")
                if (box == null || Status.valueOf(box.status) == Status.ERROR) {
                    LogMgr.e("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} failed")
                    operateBoxListener?.onFailed(null)
                } else {
                    mRobotViewModel.operateBox(boxCmd).observe(this, Observer
                    {
                        val openResponse = it.getOrNull()
                        LogMgr.i("${boxCmd.operateType.cmd} response $openResponse")
                        openResponse?.let {
                            if (openResponse.code() == 200) {
                                LogMgr.i("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} success<<<")
                                queryLockStatusLimit(4, boxCmd, operateBoxListener)
                            } else {
                                LogMgr.e("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} failed")
                                operateBoxListener?.onFailed(null)
                            }
                        } ?: let {
                            LogMgr.e("${boxCmd.operateType.cmd}  cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} failed")
                            operateBoxListener?.onFailed(null)
                        }
                    })
                }
            })
    }


    private fun queryLockStatusLimit(
        limit: Int,
        boxCmd: BoxCmd,
        operateBoxListener: OperateBoxListener
    ) {
        if (activity != null) {
            var limitTime = limit
            mRobotViewModel.queryBox(limitTime, RequestBox(boxCmd.cargo_id, boxCmd.box_id))
                .observe(this, Observer {
                    val box = it.getOrNull()
                    LogMgr.i("query box ${boxCmd.box_id} response $box")
                    limitTime--
                    if (limitTime == 0) {
                        LogMgr.i("query ${boxCmd.operateType} cargo ${boxCmd.cargo_id} box ${boxCmd.box_id} finished")
                        operateBoxListener?.onFailed(null)
                    } else if (limitTime > 0) {
                        box?.let {
                            when (LockStatus.valueOf(box.lock_status)) {
                                LockStatus.LOCKED -> {
                                    if (boxCmd.operateType == OperateType.CLOSE) {
                                        limitTime = 0
                                        operateBoxListener?.onSuccess(box)
                                    }
                                }
                                LockStatus.UNLOCKED -> {
                                    if (boxCmd.operateType == OperateType.OPEN) {
                                        limitTime = 0
                                        operateBoxListener?.onSuccess(box)
                                    }
                                }
                            }
                        } ?: let {
                            LogMgr.e("query box  ${boxCmd.box_id} failed")
                        }
                    }
                })
        }
    }


    fun queryCargos(lifecycleOwner: LifecycleOwner, queryCargosListener: QueryCargosListener) {
        mRobotViewModel.queryCargos().observe(lifecycleOwner, Observer {
            val cargos = it.getOrNull()
            cargos?.let {
                queryCargosListener.onQuery(cargos)
            } ?: queryCargosListener.onQuery(arrayListOf())

        })
    }


    fun queryIdleTakeoutBoxes(
        lifecycleOwner: LifecycleOwner,
        queryIdleTakeoutBoxesListener: QueryIdleTakeoutBoxesListener
    ) {
        queryBoxes(lifecycleOwner, CargoType.TAKEOUT, object : QueryBoxesListener {
            override fun onQuery(boxes: List<CargoBox>) {
                LogMgr.i("query  takeout boxes $boxes")
                mRobotViewModel.takeoutBoxes.clear()
                mRobotViewModel.takeoutBoxes.addAll(boxes)
                queryFailedTakeoutBoxes(lifecycleOwner, object : QueryFailedTakeoutBoxesListener {
                    override fun onQuery(failedTakeoutBoxes: List<FailedTaskBox>?) {
                        LogMgr.i("query failed takeout boxes $failedTakeoutBoxes")
                        failedTakeoutBoxes?.let {
                            mRobotViewModel.failedTakeoutBoxes.clear()
                            mRobotViewModel.failedTakeoutBoxes.addAll(failedTakeoutBoxes)
                            queryAssignedTakeoutBoxes(lifecycleOwner,
                                object : QueryAssignedTakeoutBoxesListener {
                                    override fun onQuery(assignedTakeoutBoxes: List<CargoBox>?) {
                                        LogMgr.i("query assigned takeout boxes $assignedTakeoutBoxes")
                                        assignedTakeoutBoxes?.let {
                                            mRobotViewModel.assignedTakeoutBoxes.clear()
                                            mRobotViewModel.assignedTakeoutBoxes.addAll(
                                                assignedTakeoutBoxes
                                            )
                                            var idleBoxes =
                                                mRobotViewModel.takeoutBoxes.filter { box ->
                                                    Status.valueOf(
                                                        box.box.status
                                                    ) != Status.ERROR
                                                }
                                            idleBoxes =
                                                idleBoxes.filterNot { fineBox -> mRobotViewModel.failedTakeoutBoxes.find { failedTaskBox -> fineBox.cargo_id == failedTaskBox.cargo_id } != null }
                                            idleBoxes =
                                                idleBoxes.filterNot { idleBox -> mRobotViewModel.assignedTakeoutBoxes.find { assignedBox -> idleBox.cargo_id == assignedBox.cargo_id } != null }
                                            LogMgr.i("query idle takeout boxes $idleBoxes")
                                            queryIdleTakeoutBoxesListener.onQuery(idleBoxes)
                                        }
                                    }
                                })
                        }
                    }
                })
            }
        })
    }

    fun queryAssignedTakeoutBoxes(
        lifecycleOwner: LifecycleOwner,
        assignedTakeoutBoxesListener: QueryAssignedTakeoutBoxesListener
    ) {
        mRobotViewModel.queryAssignedCargos().observe(lifecycleOwner, Observer {
            val assignedCargos = it.getOrNull()
            assignedCargos?.let {
                val assignedTakeoutBoxes = arrayListOf<CargoBox>()
                assignedCargos.forEach { assignedCargo ->
                    assignedCargo.boxes.forEach { assignedBox ->
                        assignedTakeoutBoxes.add(
                            CargoBox(
                                assignedCargo.cargo_id,
                                Cargo.Box(assignedBox)
                            )
                        )
                    }
                }
                assignedTakeoutBoxesListener.onQuery(assignedTakeoutBoxes)
            } ?: let {
                LogMgr.e("query assigned cargos failed")
                assignedTakeoutBoxesListener.onQuery(null)
            }
        })
    }

    fun queryFailedTakeoutBoxes(
        lifecycleOwner: LifecycleOwner,
        queryFailedTakeoutBoxesListener: QueryFailedTakeoutBoxesListener
    ) {
        mRobotViewModel.queryTasks(TaskStatus.FAILED.status, TaskType.TAKEOUT.type)
            .observe(lifecycleOwner, Observer {
                val tasks = it.getOrNull()
                tasks?.let {
                    val failedTakeoutBoxes = arrayListOf<FailedTaskBox>()
                    mRobotViewModel.failedTakeoutTasks.clear()
                    mRobotViewModel.failedTakeoutTasks.addAll(tasks)
                    mRobotViewModel.failedTakeoutTasks.forEach { taskInfo ->
                        taskInfo.task.cargos.forEach { cargo ->
                            cargo.boxes.forEach { taskBox ->
                                failedTakeoutBoxes.add(
                                    FailedTaskBox(
                                        taskInfo.task.target,
                                        cargo.cargo_id,
                                        Cargo.Box(taskBox)
                                    )
                                )
                            }
                        }
                    }
                    LogMgr.d("query failed takeout boxes success")
                    queryFailedTakeoutBoxesListener.onQuery(failedTakeoutBoxes)
                } ?: let {
                    LogMgr.e("query failed takeout boxes failed")
                    queryFailedTakeoutBoxesListener.onQuery(null)
                }
            })
    }



    fun queryBoxes(
        lifecycleOwner: LifecycleOwner,
        cargoType: CargoType = CargoType.TAKEOUT,
        queryBoxesListener: QueryBoxesListener
    ) {
        queryCargos(lifecycleOwner, object : QueryCargosListener {
            override fun onQuery(cargos: List<Cargo>) {
                if (cargos.isNotEmpty()) {
                    var boxes = arrayListOf<CargoBox>()
                    val filterCargos = cargos.filter { c -> CargoType.valueOf(c.type) == cargoType }
                    filterCargos.forEach { filterCargo ->
                        filterCargo.boxes.forEach { box ->
                            boxes.add(CargoBox(filterCargo.id, box))
                        }
                    }
                    queryBoxesListener.onQuery(boxes)
                } else {
                    queryBoxesListener.onQuery(arrayListOf())
                }
            }
        })
    }

    fun disableTask() {
        if (activity != null) {
            mRobotViewModel?.enableTaskExecution(TaskExecution(false)).observe(this, Observer {
                val ret = it.getOrNull()
                LogMgr.d("disable task response ${ret.toString()}")
            })
        }
    }

    fun enableTask() {
        if (activity != null) {
            mRobotViewModel?.enableTaskExecution(TaskExecution(true)).observe(activity!!, Observer {
                val ret = it.getOrNull()
                LogMgr.d("enable task response ${ret.toString()}")
            })
        }
    }


    fun closeOpenBox() {
        val cargoBox = mRobotViewModel.openBox
        cargoBox?.let {
            operateBox(
                BoxCmd(cargoBox.cargo_id, cargoBox.box.id, OperateType.CLOSE),
                object : OperateBoxListener {
                    override fun onSuccess(box: Cargo.Box) {

                    }

                    override fun onFailed(box: FailedBox?) {

                    }
                })
        }
    }
}