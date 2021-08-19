package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.*
import kotlinx.android.synthetic.main.fragment_deliver.*

class DeliverFragment : BaseFragment() {



    companion object {
        fun newInstance() = DeliverFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_deliver, container, false)
    }

    override fun initView() {
        super.initView()
        val taskType = mRobotViewModel.deliveringInfo?.task?.type
        taskType?.let {
            refreshTargetName(TaskType.valueOf(taskType))
        }
    }


    private fun refreshTargetName(taskType: TaskType) {
        tv_deliver_poi_name?.text = when (taskType) {
            TaskType.TAKEOUT, TaskType.RETAIL -> {
                getString(R.string.deliver_to_room)
            }
            TaskType.COLLECT -> {
                getString(R.string.deliver_to_reception)
            }
            TaskType.REFILL -> {
                getString(R.string.deliver_to_refill)
            }
        }
    }
}

