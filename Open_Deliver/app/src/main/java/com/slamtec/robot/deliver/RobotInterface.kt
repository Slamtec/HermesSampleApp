package com.slamtec.robot.deliver

import com.slamtec.robot.deliver.model.Cargo
import com.slamtec.robot.deliver.model.CargoBox
import com.slamtec.robot.deliver.model.FailedBox
import com.slamtec.robot.deliver.model.FailedTaskBox


interface OperateBoxListener {
    fun onSuccess(box: Cargo.Box)
    fun onFailed(failedBox: FailedBox?)
}

interface QueryCargosListener {
    fun onQuery(cargos: List<Cargo>)
}

interface QueryEmptyBoxesListener {
    fun onQuery(emptyBoxes: List<CargoBox>)
}

interface QueryBoxesListener {
    fun onQuery(boxes: List<CargoBox>)
}

interface QueryFailedTakeoutBoxesListener{
    fun onQuery(failedTakeoutBoxes:List<FailedTaskBox>?)
}

interface QueryAssignedTakeoutBoxesListener{
    fun onQuery(assignedTakeoutBoxes:List<CargoBox>?)
}

interface QueryIdleTakeoutBoxesListener{
    fun onQuery(idleTakeoutBoxes:List<CargoBox>)
}