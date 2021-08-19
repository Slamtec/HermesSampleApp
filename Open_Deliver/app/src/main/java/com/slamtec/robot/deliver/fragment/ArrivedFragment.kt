package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.DateUtils
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_arrived.*
import kotlinx.coroutines.*

class ArrivedFragment : BaseFragment() {
    private var mActionTimeoutJob: Job? = null


    companion object {
        fun newInstance() = ArrivedFragment()
    }

    enum class ViewState {
        ARRIVED, OPENING, ERROR, RETRIEVE
    }

    private var countDown = 10 * 1000L
    private var errorCountTimer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_arrived, container, false)
    }

    override fun initView() {
        super.initView()
        val arrivedTask = mRobotViewModel.taskInfo?.task
        arrivedTask?.let {
            when (TaskType.valueOf(arrivedTask.type)) {
                TaskType.COLLECT -> {
                    switchView(ViewState.RETRIEVE)
                    setActionTimeoutCheck()
                    tv_arrived_retrieve.setOnClickListener {
                        disableTask()
                        tv_arrived_retrieve.isClickable = false
                        cancelActionTimeoutJob()
                        mRobotViewModel.updateViewType(RobotViewType.RETRIEVE)
                    }
                }
                TaskType.RETAIL -> {

                }
                TaskType.TAKEOUT -> {
                    switchView(ViewState.ARRIVED)
                    tv_arrived_location?.text = arrivedTask.target
                    setActionTimeoutCheck()
                    tv_arrived_open?.setOnClickListener {
                        disableTask()
                        cancelActionTimeoutJob()
                        tv_arrived_open?.isClickable = false
                        if (arrivedTask.cargos.isNotEmpty()) {
                            switchView(ViewState.OPENING)
                            mRobotViewModel.startPickup().observe(this, androidx.lifecycle.Observer { })
                            val boxCmd = BoxCmd(arrivedTask.cargos[0].cargo_id, arrivedTask.cargos[0].boxes[0], OperateType.OPEN)
                            operateBox(boxCmd, object : OperateBoxListener {
                                override fun onSuccess(box: Cargo.Box) {
                                    LogMgr.i("open box ${box?.id} success")
                                    updateViewType(RobotViewType.TAKEOUT_PICKUP)
                                }

                                override fun onFailed(box: FailedBox?) {
                                    LogMgr.e("open box ${box?.box?.id} failed")
                                    switchView(ViewState.ERROR)
                                }
                            })
                        } else {
                            switchView(ViewState.ERROR)
                        }
                    }
                }
                TaskType.REFILL -> {
                    updateViewType(RobotViewType.HOME)
                }

            }
        } ?: let {
            disableTask()
            switchView(ViewState.ERROR)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelErrorCountTimer()
        cancelActionTimeoutJob()
    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.OPENING -> {
                rl_arrived_retrieve?.visibility = View.GONE
                rl_arrived_opening?.visibility = View.VISIBLE
                rl_arrived?.visibility = View.GONE
                rl_arrived_error.visibility = View.GONE
            }
            ViewState.ERROR -> {
                rl_arrived_retrieve?.visibility = View.GONE
                rl_arrived_opening?.visibility = View.GONE
                rl_arrived?.visibility = View.GONE
                rl_arrived_error?.visibility = View.VISIBLE
                startErrorCountTimer()
            }
            ViewState.RETRIEVE -> {
                rl_arrived_retrieve?.visibility = View.VISIBLE
                rl_arrived_opening?.visibility = View.GONE
                rl_arrived?.visibility = View.GONE
                rl_arrived_error?.visibility = View.GONE
                val failedTaskCount = mRobotViewModel.taskInfo?.task?.failed_tasks?.size ?: 1
                tv_arrived_failed_task.text = getString(R.string.failed_tip, failedTaskCount.toString())
            }
            else -> {
                rl_arrived_retrieve?.visibility = View.GONE
                rl_arrived_opening?.visibility = View.GONE
                rl_arrived?.visibility = View.VISIBLE
                rl_arrived_error?.visibility = View.GONE
            }
        }
    }

    private fun startErrorCountTimer() {
        errorCountTimer = object : CountDownTimer(countDown, 100) {
            override fun onFinish() {
                enableTask()
                updateViewType(RobotViewType.HOME)
            }

            override fun onTick(millisUntilFinished: Long) {
                val p = 100 * (countDown - millisUntilFinished) / countDown
                sv_arrived_sandglass?.setProgress(p.toInt(), 100)
                tv_arrived_sandglass?.text = getString(R.string.send_other_orders, DateUtils.formatDateSeconds(millisUntilFinished))
            }
        }
        errorCountTimer?.start()
    }

    private fun cancelErrorCountTimer() {
        errorCountTimer?.cancel()
    }

    private fun setActionTimeoutCheck() {
        mActionTimeoutJob?.cancel()
        mActionTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000-1000)
            enableTask()
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelActionTimeoutJob() {
        mActionTimeoutJob?.cancel()
    }




}