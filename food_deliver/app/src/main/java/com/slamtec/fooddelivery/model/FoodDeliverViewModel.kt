package com.slamtec.fooddelivery.model

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slamtec.fooddelivery.api.Repository

/**
 * File   : FoodDeliverViewModel
 * Author : Qikun.Xiong
 * Date   : 2021/8/10 4:07 PM
 */
class FoodDeliverViewModel : ViewModel() {
    var powerStatus: PowerStatus? = null
    private var preViewType: ViewType? = null
    val viewType = MutableLiveData<ViewType>()
    var mRoomList = arrayListOf<String>()

    var plate1 = PlateInfo()
    var plate2 = PlateInfo()
    var plate3 = PlateInfo()
    var plate4 = PlateInfo()
    var deliveringPlate: PlateInfo? = null
    var refillPoi: String? = null
    var taskResult: Boolean? = null
    var robotHealth = MutableLiveData<RobotHealth>()
    var deviceInfo: DeviceInfo? = null
    var currentSpeed: String? = null

    fun updateViewType(type: ViewType) {
        if (viewType.value != type) {
            preViewType = viewType.value
            viewType.value = type
        }
    }

    fun initPlate() {
        taskResult = true
        plate1 = PlateInfo(plateName = "1L")
        plate2 = PlateInfo(plateName = "2L")
        plate3 = PlateInfo(plateName = "3L")
        plate4 = PlateInfo(plateName = "4L")
    }

    fun backView() {
        viewType.value = preViewType
    }
}