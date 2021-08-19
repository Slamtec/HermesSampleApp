package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.QueryIdleTakeoutBoxesListener
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.agentservice.QueryEmptyBoxesListener
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_box_close.*
import kotlinx.coroutines.*

class BoxCloseFragment : BaseFragment() {
    private var mFirstCloseFailedDelayJob: Job? = null
    private var mSecondCloseFailedDelayJob: Job? = null

    enum class ViewState {
        CLOSING, FAILED, SECOND_FAILED
    }

    companion object {
        fun newInstance() = BoxCloseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_box_close, container, false)
    }

    override fun initView() {
        super.initView()
        tv_box_close_failed_home.setOnClickListener {
            cancelFirstCloseFailedDelayJob()
            updateViewType(RobotViewType.HOME)
        }
        tv_box_close_failed_retry.setOnClickListener {
            cancelFirstCloseFailedDelayJob()
            updateViewType(RobotViewType.INPUT)
        }
        tv_second_box_close_failed.setOnClickListener {
            cancelSecondCloseFailedDelayJob()
            updateViewType(RobotViewType.SEND)
        }
        switchView(ViewState.CLOSING)
        if (mRobotViewModel.openBox != null) {
            val boxCmd = BoxCmd(
                mRobotViewModel.openBox!!.cargo_id,
                mRobotViewModel.openBox!!.box.id,
                OperateType.CLOSE
            )
            operateBox(boxCmd, object : OperateBoxListener {
                override fun onSuccess(box: Cargo.Box) {
                    LogMgr.i("close box ${box?.id} success")
                    mRobotViewModel.openBox!!.box.door_status = BoxState.CLOSED.toString()
                    updateViewType(RobotViewType.CREATE)
                }

                override fun onFailed(box: FailedBox?) {
                    LogMgr.e("close box ${box?.box?.id} failed")
                    val viewState =
                        if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_FAILED else ViewState.FAILED
                    switchView(viewState)
                }

            })
        }
        queryIdleTakeoutBoxes(this, object : QueryIdleTakeoutBoxesListener {
            override fun onQuery(idleTakeoutBoxes: List<CargoBox>) {
                mRobotViewModel.idleBoxes.clear()
                mRobotViewModel.idleBoxes.addAll(idleTakeoutBoxes)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelFirstCloseFailedDelayJob()
        cancelSecondCloseFailedDelayJob()
    }


    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.CLOSING -> {
                rl_box_closing?.visibility = View.VISIBLE
                rl_close_box_failed?.visibility = View.GONE
                rl_close_second_box_failed?.visibility = View.GONE
            }
            ViewState.SECOND_FAILED -> {
                rl_box_closing?.visibility = View.GONE
                rl_close_box_failed?.visibility = View.GONE
                rl_close_second_box_failed?.visibility = View.VISIBLE
                setSecondCloseFailedDelayCheck()
            }
            else -> {
                rl_box_closing?.visibility = View.GONE
                rl_close_box_failed?.visibility = View.VISIBLE
                rl_close_second_box_failed?.visibility = View.GONE
                tv_box_close_failed_retry?.visibility =
                    if (mRobotViewModel.idleBoxes.size > 0) View.VISIBLE else View.GONE
                setFirstCloseFailedDelayCheck()
            }
        }
    }

    private fun setFirstCloseFailedDelayCheck() {
        mFirstCloseFailedDelayJob?.cancel()
        mFirstCloseFailedDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelFirstCloseFailedDelayJob() {
        mFirstCloseFailedDelayJob?.cancel()
    }

    private fun setSecondCloseFailedDelayCheck() {
        mSecondCloseFailedDelayJob?.cancel()
        mSecondCloseFailedDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            updateViewType(RobotViewType.SEND)
        }
    }

    private fun cancelSecondCloseFailedDelayJob() {
        mSecondCloseFailedDelayJob?.cancel()
    }

}