package com.slamtec.robot.deliver.agentservice

import com.slamtec.robot.deliver.model.Cargo
import com.slamtec.robot.deliver.model.CargoBox
import com.slamtec.robot.deliver.model.FailedBox

interface OperateBoxListener {
    fun onSuccess(box: Cargo.Box)
    fun onFailed(failedBox: FailedBox?)
}

interface QueryEmptyBoxesListener {
    fun onQuery(emptyBoxes: List<CargoBox>)
}