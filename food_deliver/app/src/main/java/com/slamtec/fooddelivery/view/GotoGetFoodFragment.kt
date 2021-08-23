package com.slamtec.fooddelivery.view

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
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_go_to_getfood.*
import kotlinx.android.synthetic.main.layout_robot.*

/**
 * File   : GotoGetFoodFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/12 3:53 PM
 */
class GotoGetFoodFragment : BasePlateFragment() {

    companion object {
        fun newInstance() =
                GotoGetFoodFragment()
    }

    private val mHandler = Handler {
        Repository.stopCurrentAction()
        //当前任务超时，执行下一个任务
        tv_deliver_tips.text = "回取餐点超时...马上返回主界面"
        updateViewType(ViewType.HOME)
        false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_go_to_getfood, container, false)
    }

    override fun initView() {
        super.initView()
        queryRobotHealth()
        setOnDeliveringPlateText(mViewModel.plate1, tv_1)
        setOnDeliveringPlateText(mViewModel.plate2, tv_2)
        setOnDeliveringPlateText(mViewModel.plate3, tv_3)
        setOnDeliveringPlateText(mViewModel.plate4, tv_4)
        if (mViewModel.taskResult!!) {
            tv_deliver_tips.text = "本次任务已完成，我要回取餐点啦..."
        } else {
            tv_deliver_tips.text = "很遗憾，本次任务未完成，先回取餐点啦..."
        }
        executeDeliveringTask()
    }

    private fun executeDeliveringTask() {
        //回取餐点
        val action = CreateMoveAction(Constants.MOVE_ACTION_NAME, CreateMoveAction.Options(CreateMoveAction
                .Options.MoveTarget(mViewModel.refillPoi), CreateMoveAction.Options
                .MoveOptions(arrayListOf("with_yaw", "precise", "fail_retry_count"), 4)))
        LogMgr.d("goto get food : ${mViewModel.refillPoi} , action: $action")

        Repository.createNewAction(action).observe(this, Observer {
            val result = it.getOrNull()
            LogMgr.i("createNewAction response:$result")
            result?.let {
                if (result.state.result == 0) {
                    LogMgr.i("创建任务成功，开始执行")
                    //开始查询任务执行状态
                    val actionId = result.action_id
                    Repository.getActionStatus(actionId).observe(this, Observer { getRp ->
                        val getRps = getRp.getOrNull()
                        LogMgr.d("getRps:$getRps")
                        if (getRps != null) {
                            if (getRps.state.status == 4) {
                                //回取餐点，回首页去
                                updateViewType(ViewType.HOME)
                            }
                        }
                    })
                    mHandler.sendEmptyMessageDelayed(0, 120 * 1000)
                } else {
                    ToastTool.showWarningToast(context!!, "启动失败,返回主界面", 0, 30)
                    mHandler.sendEmptyMessage(0)
                    LogMgr.e("任务启动失败，回取餐点失败")
                }
            } ?: let {
                mHandler.sendEmptyMessage(0)
                ToastTool.showWarningToast(context!!, "启动失败,返回主界面", 0, 30)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }
}