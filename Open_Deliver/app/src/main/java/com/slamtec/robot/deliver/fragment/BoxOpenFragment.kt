package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.QueryIdleTakeoutBoxesListener
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_box_open.*
import kotlinx.coroutines.*

class BoxOpenFragment : BaseFragment() {
    private var mFirstOpenFailedDelayJob: Job? = null
    private var mSecondOpenFailedDelayJob: Job? = null

    enum class ViewState {
        OPENING, FAILED, SECOND_FAILED, NO_BOX, SECOND_NO_BOX
    }

    companion object {
        fun newInstance() = BoxOpenFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_box_open, container, false)
    }

    override fun initView() {
        super.initView()
        tv_box_no_box.setOnClickListener {
            updateViewType(RobotViewType.HOME)
        }
        tv_box_failed_home.setOnClickListener {
            cancelFirstOpenFailedDelayJob()
            updateViewType(RobotViewType.HOME)
        }
        tv_box_failed_retry.setOnClickListener {
            cancelFirstOpenFailedDelayJob()
            updateViewType(RobotViewType.INPUT)
        }
        tv_second_box_open_failed.setOnClickListener {
            cancelSecondOpenFailedDelayJob()
            updateViewType(RobotViewType.SEND)
        }
        tv_box_open_second_no_box.setOnClickListener {
            updateViewType(RobotViewType.SEND)
        }
        queryIdleTakeoutBoxes(this, object : QueryIdleTakeoutBoxesListener {
            override fun onQuery(idleTakeoutBoxes: List<CargoBox>) {
                mRobotViewModel.idleBoxes.clear()
                mRobotViewModel.idleBoxes.addAll(idleTakeoutBoxes)
                if (idleTakeoutBoxes != null && idleTakeoutBoxes.isNotEmpty()) {
                    operateBox(
                        BoxCmd(
                            idleTakeoutBoxes[0].cargo_id,
                            idleTakeoutBoxes[0].box.id,
                            OperateType.OPEN
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                LogMgr.i("open box ${box?.id} success")
                                mRobotViewModel.openBox!!.box.door_status = BoxState.OPEN.toString()
                                updateViewType(RobotViewType.SEND)
                            }

                            override fun onFailed(box: FailedBox?) {
                                LogMgr.e("open box ${box?.box?.id} failed")
                                val viewState =
                                    if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_FAILED else ViewState.FAILED
                                switchView(viewState)
                            }
                        })
                    mRobotViewModel.openBox = idleTakeoutBoxes[0]
                } else {
                    val viewState =
                        if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_NO_BOX else ViewState.NO_BOX
                    switchView(viewState)
                    LogMgr.e("no empty cargos")
                }
            }
        })
        switchView(ViewState.OPENING)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelFirstOpenFailedDelayJob()
        cancelSecondOpenFailedDelayJob()
    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.OPENING -> {
                rl_open_second_no_box?.visibility = View.GONE
                rl_box_no_box?.visibility = View.GONE
                rl_box_opening?.visibility = View.VISIBLE
                rl_open_box_failed?.visibility = View.GONE
                rl_open_second_box_failed?.visibility = View.GONE
            }
            ViewState.SECOND_FAILED -> {
                rl_open_second_no_box?.visibility = View.GONE
                rl_box_no_box?.visibility = View.GONE
                rl_box_opening?.visibility = View.GONE
                rl_open_box_failed?.visibility = View.GONE
                rl_open_second_box_failed?.visibility = View.VISIBLE
                setSecondOpenFailedDelayCheck()
            }
            ViewState.NO_BOX -> {
                rl_open_second_no_box?.visibility = View.GONE
                rl_box_no_box?.visibility = View.VISIBLE
                rl_box_opening?.visibility = View.GONE
                rl_open_box_failed?.visibility = View.GONE
                rl_open_second_box_failed?.visibility = View.GONE
            }
            ViewState.SECOND_NO_BOX -> {
                rl_open_second_no_box?.visibility = View.VISIBLE
                rl_box_no_box?.visibility = View.GONE
                rl_box_opening?.visibility = View.GONE
                rl_open_box_failed?.visibility = View.GONE
                rl_open_second_box_failed?.visibility = View.GONE
            }
            else -> {
                rl_open_second_no_box?.visibility = View.GONE
                rl_box_no_box?.visibility = View.GONE
                rl_box_opening?.visibility = View.GONE
                rl_open_second_box_failed?.visibility = View.GONE
                rl_open_box_failed?.visibility = View.VISIBLE
                tv_box_failed_retry?.visibility =
                    if (mRobotViewModel.idleBoxes.size > 1) View.VISIBLE else View.GONE
                setFirstOpenFailedDelayCheck()
            }
        }
    }

    private fun setFirstOpenFailedDelayCheck() {
        mFirstOpenFailedDelayJob?.cancel()
        mFirstOpenFailedDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelFirstOpenFailedDelayJob() {
        mFirstOpenFailedDelayJob?.cancel()
    }

    private fun setSecondOpenFailedDelayCheck() {
        mSecondOpenFailedDelayJob?.cancel()
        mSecondOpenFailedDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            updateViewType(RobotViewType.SEND)
        }
    }

    private fun cancelSecondOpenFailedDelayJob() {
        mSecondOpenFailedDelayJob?.cancel()
    }
}