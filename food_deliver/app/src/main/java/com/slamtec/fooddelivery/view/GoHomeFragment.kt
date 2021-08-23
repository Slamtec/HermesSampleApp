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
import com.slamtec.fooddelivery.utils.SoundPoolUtil
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_go_home.*

/**
 * File   : GoHomeFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/12 5:26 PM
 */
class GoHomeFragment : BasePlateFragment() {
    companion object {
        fun newInstance() =
                GoHomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_go_home, container, false)
    }

    private val mHandler = Handler {
        Repository.stopCurrentAction()
        //当前任务超时，执行下一个任务
        tv_tips.text = "回取餐点超时...马上返回主界面"
        updateViewType(ViewType.HOME)
        false
    }

    override fun initView() {
        super.initView()
        queryRobotHealth()
        //回桩
        SoundPoolUtil.getInstance(context!!).playSoundWithRedId(R.raw.goto_charing)
        val action = CreateMoveAction(Constants.BACK_HOME_NAME, null)
        LogMgr.d("go home, action: $action")
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
                    mHandler.sendEmptyMessage(0)
                    ToastTool.showWarningToast(context!!, "任务启动失败，回桩失败", 0, 30)
                    LogMgr.e("任务启动失败，回桩失败")
                }
            } ?: let {
                mHandler.sendEmptyMessage(0)
                LogMgr.e("启动失败")
                ToastTool.showWarningToast(context!!, "启动失败", 0, 30)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }
}