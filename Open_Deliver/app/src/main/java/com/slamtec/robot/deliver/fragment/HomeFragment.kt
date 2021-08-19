package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.slamtec.robot.deliver.QueryIdleTakeoutBoxesListener
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.agentservice.QueryEmptyBoxesListener
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*


class HomeFragment : BaseFragment() {
    private var mDelayJob: Job? = null

    companion object {
        fun newInstance() = HomeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadQrcodeView(mRobotViewModel.resource?.qr_code?.url)
    }

    private fun loadQrcodeView(url: String?) {
        Glide.with(context!!).load(url).override(180, 180)
            .error(R.drawable.hs_code).placeholder(R.drawable.hs_code).fallback(R.drawable.hs_code)
            .fitCenter().into(iv_home_qrcode)
    }


    override fun initView() {
        super.initView()
        iv_home_setting.setOnClickListener {
            disableTask()
            updateViewType(RobotViewType.SETTING)
        }
        iv_home_send.setOnClickListener {
            disableTask()
            val batteryPercentage = mRobotViewModel.powerStatus?.batteryPercentage ?: 0
            if (batteryPercentage > 30) {
                queryIdleTakeoutBoxes(this, object : QueryIdleTakeoutBoxesListener {
                    override fun onQuery(idleTakeoutBoxes: List<CargoBox>) {
                        mRobotViewModel.idleBoxes.clear()
                        mRobotViewModel.idleBoxes.addAll(idleTakeoutBoxes)
                        mRobotViewModel.updateViewType(RobotViewType.INPUT)
                    }
                })
            } else {
                mRobotViewModel.updateViewType(RobotViewType.LOW_BATTERY)
            }
        }

        mRobotViewModel.queryEvents().observe(this, Observer {
            val events = it.getOrNull()
            LogMgr.i("events $events")

        })
        val cargoBox = mRobotViewModel.openBox
        cargoBox?.let {
            operateBox(
                BoxCmd(cargoBox.cargo_id, cargoBox.box.id, OperateType.CLOSE),
                object : OperateBoxListener {
                    override fun onSuccess(box: Cargo.Box) {

                    }

                    override fun onFailed(box: FailedBox?) {

                    }
                })
        }
        refreshConfigurations()
        checkDelayJob()
    }


    override fun onDestroy() {
        super.onDestroy()
        mDelayJob?.cancel()
    }

    private fun checkDelayJob() {
        mDelayJob?.cancel()
        mDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(2 * 60 * 1000)
            enableTask()
        }
    }

    private fun refreshConfigurations() {
        mRobotViewModel.queryConfigInfo().observe(this, Observer {
            val configurations = it.getOrNull()
            LogMgr.i("configurations $configurations")
            configurations?.let {
                mRobotViewModel.configurations = configurations
            }
        })
    }



}