package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.*
import kotlinx.android.synthetic.main.fragment_low_battery.*
import kotlinx.coroutines.*

class LowBatteryFragment : BaseFragment() {
    private var mTimeoutJob: Job? = null

    companion object {
        fun newInstance() = LowBatteryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_low_battery, container, false)
    }

    override fun initView() {
        super.initView()
        tv_low_battery_home.setOnClickListener {
            updateViewType(RobotViewType.HOME)
        }
        setTimeoutCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimeoutJob()
    }


    private fun setTimeoutCheck() {
        mTimeoutJob?.cancel()
        mTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(2 * 60 * 1000)
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelTimeoutJob() {
        mTimeoutJob?.cancel()
    }

}