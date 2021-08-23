package com.slamtec.fooddelivery.view

import android.annotation.SuppressLint
import android.widget.TextView
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.model.PlateInfo
import com.slamtec.fooddelivery.utils.LogMgr

/**
 * File   : BaseDeliveringFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/12 10:27 AM
 */
abstract class BasePlateFragment : BaseFragment() {

    fun isBatteryLow(): Boolean {
        val batteryPercentage = mViewModel.powerStatus?.batteryPercentage ?: 0
        LogMgr.d("batter : $batteryPercentage")
        return batteryPercentage < 20
    }

    @SuppressLint("SetTextI18n")
    fun setOnDeliveringPlateText(plateInfo: PlateInfo, tv: TextView) {
        when (plateInfo.status) {
            0 -> {
                tv.text = plateInfo.plateName + " - 未放置"
                tv.setBackgroundResource(R.drawable.robot_tray_l02_unselected)
            }
            1 -> {
                tv.text = plateInfo.plateName + " - 未放置"
                tv.setBackgroundResource(R.drawable.robot_tray_l02_unselected)
            }
            2, 3, 5, 6 -> {
                tv.text = plateInfo.plateName + " - ${plateInfo.selectedTable}待取餐"
                tv.setBackgroundResource(R.drawable.robot_tray_l02_selected)
            }
            4 -> {
                tv.text = plateInfo.plateName + " - 已取餐"
                tv.setBackgroundResource(R.drawable.robot_tray_l02_unselected)
            }
        }
    }
}