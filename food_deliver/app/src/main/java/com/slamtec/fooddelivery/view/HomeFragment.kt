package com.slamtec.fooddelivery.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_robot.*


/**
 * File   : HomeFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/11 11:24 AM
 */
class HomeFragment : BasePlateFragment() {

    companion object {
        fun newInstance() =
                HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun initView() {
        super.initView()
        LogMgr.d("taskResult:${mViewModel.taskResult}")
        mViewModel.taskResult?.let {
            //任务已完成
            if (it) {
                deliver_again.visibility = View.GONE
            } else {
                deliver_again.visibility = View.VISIBLE
            }
        } ?: let {
            //任务未完成 或者 没有任务
            deliver_again.visibility = View.GONE
        }
        deliver_again.setOnClickListener {
            updateViewType(ViewType.INPUT_TABLE)
        }
        setOnDeliveringPlateText(mViewModel.plate1, tv_1)
        setOnDeliveringPlateText(mViewModel.plate2, tv_2)
        setOnDeliveringPlateText(mViewModel.plate3, tv_3)
        setOnDeliveringPlateText(mViewModel.plate4, tv_4)
        deliver.setOnClickListener {
            //清除之前的所有任务，初始化任务
            mViewModel.initPlate()
            //todo just for test
//            updateViewType(ViewType.INPUT_TABLE)
            mViewModel.refillPoi?.let {
                if (isBatteryLow()) {
                    LogMgr.e("电量不足，请充电")
                    ToastTool.showWarningToast(context!!, "电量不足，请充电！", 0, 30)
                } else {
                    updateViewType(ViewType.INPUT_TABLE)
                }
            } ?: let {
                ToastTool.showWarningToast(context!!, "当前地图未设置取餐点，请先设置取餐点", 0, 30)
            }
        }
        cl_setting.setOnClickListener {
            updateViewType(ViewType.SETTING)
        }
    }
}