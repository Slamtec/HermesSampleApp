package com.slamtec.fooddelivery.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.constants.Constants
import com.slamtec.fooddelivery.model.PlateInfo
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.SoundPoolUtil
import kotlinx.android.synthetic.main.fragment_arrived.*
import kotlinx.android.synthetic.main.layout_robot.*

/**
 * File   : ArrivedFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/12 11:50 AM
 */
class ArrivedFragment : BasePlateFragment() {
    companion object {
        fun newInstance() =
                ArrivedFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_arrived, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        setOnDeliveringPlateText(mViewModel.plate1, tv_1)
        setOnDeliveringPlateText(mViewModel.plate2, tv_2)
        setOnDeliveringPlateText(mViewModel.plate3, tv_3)
        setOnDeliveringPlateText(mViewModel.plate4, tv_4)
        queryRobotHealth()
        SoundPoolUtil.getInstance(context!!).playSoundUnfinished(R.raw.get_food)
        tv_tips.text = mViewModel.deliveringPlate?.selectedTable + "请取" + mViewModel.deliveringPlate?.plateName
        tv_have_taken.setOnClickListener {
            //把当前餐盘的状态置为已取餐
            val plate = plates.find { it.plateName == mViewModel.deliveringPlate?.plateName }
            plate?.status = Constants.HAVE_TAKEN_FOOD
            LogMgr.d("Take plate:$plate")
            //判断是否还有任务未完成
            val plateNotComplete = plates.find {
                it.selectedTable != "null" && (it.status == Constants.CHOSEN_TABLE)
            }
            LogMgr.d("platNotComplte:$plateNotComplete")
            plateNotComplete?.let {
                //任务未完成，继续派送
                LogMgr.d("任务未完成，继续派送")
                updateViewType(ViewType.DELIVERING)
            } ?: let {
                SoundPoolUtil.getInstance(context!!).playSoundWithRedId(R.raw.completed_task)
                //任务全部完成了,回取餐点
                val finalPlate = plates.find {
                    it.status == Constants.CHOSEN_TABLE
                            || it.status == Constants.TAKEN_FOOD_TIME_OUT || it.status == Constants.DELIVERING_FOOD_TIME_OUT
                }
                mViewModel.taskResult = finalPlate == null
                LogMgr.i("最终任务完成：${mViewModel.taskResult}")
                updateViewType(ViewType.GOTO_GET_FOOD)
            }
        }
    }

    override fun onSetCountDownTime(): Int {
        return 121
    }

    override fun onCountDownTick(millisUntilFinished: Long, finishedPercent: Int, untilFinishedDate: String) {
        super.onCountDownTick(millisUntilFinished, finishedPercent, untilFinishedDate)
        val count = (millisUntilFinished / 1000).toInt()
        if (count <= 120) {
            count_down_time.visibility = View.VISIBLE
            count_down_time.text = "倒计时 : ${count}s"
        } else {
            count_down_time.visibility = View.GONE
        }
    }

    override fun onCountDownFinish() {
        super.onCountDownFinish()
        //倒计时结束,状态置为超时
        LogMgr.d("mViewModel.deliveringPlate:${mViewModel.deliveringPlate}")
        val plate = plates.find { mViewModel.deliveringPlate?.plateName == it.plateName }
        plate?.status = Constants.TAKEN_FOOD_TIME_OUT
        mViewModel.deliveringPlate?.status = Constants.TAKEN_FOOD_TIME_OUT
        //判断是否还有任务
        //todo 是否还有任务 需要考虑再考虑条件
        val deliveringPlate = plates.find { it.selectedTable != "null" && it.status == Constants.CHOSEN_TABLE }
        LogMgr.d("deliveringPlate:$deliveringPlate")
        if (deliveringPlate == null) {
            mViewModel.taskResult = false
            updateViewType(ViewType.GOTO_GET_FOOD)
        } else {
            updateViewType(ViewType.DELIVERING)
        }
    }
}