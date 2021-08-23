package com.slamtec.fooddelivery.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.api.Repository
import com.slamtec.fooddelivery.constants.Constants
import com.slamtec.fooddelivery.model.CreateMoveAction
import com.slamtec.fooddelivery.model.PlateInfo
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_delivering.*
import kotlinx.android.synthetic.main.layout_robot.*

/**
 * File   : DeliveringFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/11 7:01 PM
 */
class DeliveringFragment : BasePlateFragment() {

    companion object {
        fun newInstance() =
                DeliveringFragment()
    }

    private val mHandler = Handler {
        Repository.stopCurrentAction()
        mViewModel.deliveringPlate?.status = Constants.DELIVERING_FOOD_TIME_OUT
        val lastPlate = plates.find { it.plateName == mViewModel.deliveringPlate?.plateName }
        lastPlate?.status = Constants.DELIVERING_FOOD_TIME_OUT
        LogMgr.d("lastPlate:$$lastPlate")
        val deliveringPlate = plates.find { it.status == Constants.CHOSEN_TABLE }
        if (deliveringPlate != null) {
            //当前任务超时，执行下一个任务
            tv_deliver_tips.text = "已超时，将执行下一个任务"
            executeDeliveringTask()
        } else {
            //最后一个任务超时，回取餐点
            mViewModel.taskResult = false
            updateViewType(ViewType.GOTO_GET_FOOD)
        }
        false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delivering, container, false)
    }

    override fun initView() {
        super.initView()
        executeDeliveringTask()
        setOnDeliveringPlateText(mViewModel.plate1, tv_1)
        setOnDeliveringPlateText(mViewModel.plate2, tv_2)
        setOnDeliveringPlateText(mViewModel.plate3, tv_3)
        setOnDeliveringPlateText(mViewModel.plate4, tv_4)
        queryRobotHealth()
    }

    @SuppressLint("SetTextI18n")
    private fun executeDeliveringTask() {
        val deliveringPlate = plates.find { it.selectedTable != "null" && it.status == Constants.CHOSEN_TABLE }
        val action = CreateMoveAction(Constants.MOVE_ACTION_NAME, CreateMoveAction.Options(CreateMoveAction
                .Options.MoveTarget(deliveringPlate?.selectedTable), CreateMoveAction.Options
                .MoveOptions(arrayListOf("with_yaw", "precise", "fail_retry_count"), 4)))
        LogMgr.d("deliveringPlate: $deliveringPlate , action: $action")

        Repository.createNewAction(action).observe(this, Observer {
            val result = it.getOrNull()
            mViewModel.deliveringPlate = deliveringPlate
            LogMgr.i("createNewAction response:$result")
            result?.let {
                if (result.state.result == 0) {
                    LogMgr.i("创建任务成功，开始执行")
                    tv_deliver_tips.text = mViewModel.deliveringPlate?.selectedTable + "-配送任务执行中..."
                    //开始查询任务执行状态
                    val actionId = result.action_id
                    Repository.getActionStatus(actionId).observe(this, Observer { getRp ->
                        val getRps = getRp.getOrNull()
                        LogMgr.d("getRps:$getRps")
                        if (getRps != null) {
                            if (getRps.state.status == 4) {
                                //当前任务完成了
                                deliveringPlate?.status = Constants.ARRIVED_TABLE
                                when (deliveringPlate?.plateName) {
                                    "1L" -> {
                                        setOnDeliveringPlateText(deliveringPlate, tv_1)
                                    }
                                    "2L" -> {
                                        setOnDeliveringPlateText(deliveringPlate, tv_2)
                                    }
                                    "3L" -> {
                                        setOnDeliveringPlateText(deliveringPlate, tv_3)
                                    }
                                    "4L" -> {
                                        setOnDeliveringPlateText(deliveringPlate, tv_4)
                                    }
                                }
                                //跳转到任务完成界面
                                updateViewType(ViewType.ARRIVED_TABLE)
                            }
                        }
                    })
                    //并开始计时，超过120秒还没有结果就取消当前任务
                    mHandler.sendEmptyMessageDelayed(0, 1000 * 120)
                } else {
                    LogMgr.e("创建任务失败")
                    mHandler.sendEmptyMessage(0)
                    ToastTool.showWarningToast(context!!, "启动失败", 0, 30)
                }
            } ?: let {
                LogMgr.e("创建任务失败")
                mHandler.sendEmptyMessage(0)
                ToastTool.showWarningToast(context!!, "启动失败", 0, 30)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }
}