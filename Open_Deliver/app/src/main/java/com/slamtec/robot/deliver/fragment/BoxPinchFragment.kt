package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.RobotViewType
import kotlinx.android.synthetic.main.fragment_box_pinch.*
import kotlinx.coroutines.*

class BoxPinchFragment : BaseFragment() {
    private var mTimeoutJob: Job? = null


    companion object {
        fun newInstance() = BoxPinchFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_box_pinch, container, false)
    }

    override fun initView() {
        super.initView()
        tv_box_pinch_continue.setOnClickListener {
            enableTask()
            updateViewType(RobotViewType.GOING_HOME)
        }
        disableTask()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelTimeoutJob()
    }

    private fun cancelTimeoutJob() {
        mTimeoutJob?.cancel()
    }
}