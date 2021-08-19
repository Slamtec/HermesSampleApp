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
import kotlinx.android.synthetic.main.fragment_takeout_pickup.*

class TakeoutPickupFragment : BaseFragment() {
    private var pickupCountDownTimer: CountDownTimer? = null
    private var errorCountDownTimer: CountDownTimer? = null
    private var finishCountDownTimer: CountDownTimer? = null


    companion object {
        fun newInstance() = TakeoutPickupFragment()
    }

    enum class ViewState {
        CLOSING, ERROR, FINISH
    }

    private var errorCountDown = 10 * 1000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_takeout_pickup, container, false)
    }

    override fun initView() {
        super.initView()
        val arrivedTask = mRobotViewModel.taskInfo?.task
        arrivedTask?.let {
            tv_takeout_pickup_location?.text = arrivedTask.target
            if (!arrivedTask.no_pickup_wait) {
                sv_takeout_pickup_sandglass.visibility = View.VISIBLE
                tv_takeout_pickup_sandglass.visibility = View.VISIBLE
                var time = mRobotViewModel.resource?.delivery_waiting_time_in_secs
                if (time == null || time <= 0) {
                    time = 90
                }
                startPickupCountDownTimer(time)
            } else {
                sv_takeout_pickup_sandglass.visibility = View.INVISIBLE
                tv_takeout_pickup_sandglass.visibility = View.INVISIBLE
            }
            tv_takeout_pickup_close?.setOnClickListener {
                tv_takeout_pickup_close.isClickable = false
                cancelPickupCountDownTimer()
                switchView(ViewState.CLOSING)
                val boxCmd = BoxCmd(
                    arrivedTask.cargos[0].cargo_id,
                    arrivedTask.cargos[0].boxes[0],
                    OperateType.CLOSE
                )
                operateBox(boxCmd, object : OperateBoxListener {
                    override fun onSuccess(box: Cargo.Box) {
                        LogMgr.i("close box ${box?.id} success")
                        switchView(ViewState.FINISH)
                    }

                    override fun onFailed(box: FailedBox?) {
                        LogMgr.e("close box ${box?.box?.id} failed")
                        val failedReason = box?.reason
                        if (failedReason != null && failedReason == BoxOperationResult.Reason.CLOSE_DOOR_STALLED.toString()) {
                            disableTask()
                            updateViewType(RobotViewType.PINCH_ERROR)
                        } else {
                            switchView(ViewState.ERROR)
                        }
                    }
                })
            }
        } ?: switchView(ViewState.ERROR)
    }


    override fun onDestroy() {
        super.onDestroy()
        cancelPickupCountDownTimer()
        cancelErrorCountDownTimer()
        cancelFinishCountDownTimer()
    }

    private fun startPickupCountDownTimer(time: Int) {
        val countDown = (time * 1000).toLong()
        pickupCountDownTimer = object : CountDownTimer(countDown, 100) {
            override fun onFinish() {
                val arrivedTask = mRobotViewModel.taskInfo?.task
                arrivedTask?.let {
                    val boxCmd = BoxCmd(
                        arrivedTask.cargos[0].cargo_id,
                        arrivedTask.cargos[0].boxes[0],
                        OperateType.CLOSE
                    )
                    operateBox(boxCmd, object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                        }

                        override fun onFailed(box: FailedBox?) {
                            LogMgr.e("close box ${box?.box?.id} failed")
                            val failedReason = box?.reason
                            if (failedReason != null && failedReason == BoxOperationResult.Reason.CLOSE_DOOR_STALLED.toString()) {
                                disableTask()
                                updateViewType(RobotViewType.PINCH_ERROR)
                            } else {
                                switchView(ViewState.ERROR)
                            }
                        }

                    })
                }
                switchView(ViewState.FINISH)
            }

            override fun onTick(millisUntilFinished: Long) {
                val p = 100 * (countDown - millisUntilFinished) / countDown
                sv_takeout_pickup_sandglass?.setProgress(p.toInt(), 100)
                tv_takeout_pickup_sandglass?.text = DateUtils.formatDate(millisUntilFinished)
            }
        }
        pickupCountDownTimer?.start()
    }

    private fun cancelPickupCountDownTimer() {
        pickupCountDownTimer?.cancel()
    }


    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.CLOSING -> {
                rl_takeout_pickup_closing?.visibility = View.VISIBLE
                rl_takeout_pickup?.visibility = View.GONE
                rl_takeout_pickup_error?.visibility = View.GONE
                rl_takeout_pickup_finish?.visibility = View.GONE
            }
            ViewState.ERROR -> {
                rl_takeout_pickup_closing?.visibility = View.GONE
                rl_takeout_pickup?.visibility = View.GONE
                rl_takeout_pickup_error?.visibility = View.VISIBLE
                rl_takeout_pickup_finish?.visibility = View.GONE
                startErrorCountDownTimer()
            }
            ViewState.FINISH -> {
                rl_takeout_pickup_closing?.visibility = View.GONE
                rl_takeout_pickup?.visibility = View.GONE
                rl_takeout_pickup_error?.visibility = View.GONE
                rl_takeout_pickup_finish?.visibility = View.VISIBLE
                startFinishCountDownTimer()
            }
            else -> {
                rl_takeout_pickup_closing?.visibility = View.GONE
                rl_takeout_pickup?.visibility = View.VISIBLE
                rl_takeout_pickup_error?.visibility = View.GONE
                rl_takeout_pickup_finish?.visibility = View.GONE
            }
        }
    }

    private fun startErrorCountDownTimer() {
        errorCountDownTimer = object : CountDownTimer(errorCountDown, 100) {
            override fun onFinish() {
                enableTask()
                updateViewType(RobotViewType.HOME)
                LogMgr.i("errorCountDownTimer timer finish")
            }

            override fun onTick(millisUntilFinished: Long) {
                val p = 100 * (errorCountDown - millisUntilFinished) / errorCountDown
                sv_takeout_pickup_error_sandglass?.setProgress(p.toInt(), 100)
                tv_takeout_pickup_error_sandglass?.text = getString(
                    R.string.send_other_orders,
                    DateUtils.formatDateSeconds(millisUntilFinished)
                )
            }
        }
        errorCountDownTimer?.start()
    }

    private fun cancelErrorCountDownTimer() {
        errorCountDownTimer?.cancel()
    }

    private fun startFinishCountDownTimer() {
        finishCountDownTimer = object : CountDownTimer(errorCountDown, 100) {
            override fun onFinish() {
                enableTask()
                updateViewType(RobotViewType.HOME)
                //mRobotViewModel.taskInfo = null
                LogMgr.i("finish countDownTimer timer finish")
            }

            override fun onTick(millisUntilFinished: Long) {
                val p = 100 * (errorCountDown - millisUntilFinished) / errorCountDown
                sv_takeout_pickup_finish_sandglass?.setProgress(p.toInt(), 100)
                tv_takeout_pickup_finish_sandglass?.text = getString(
                    R.string.send_other_orders,
                    DateUtils.formatDateSeconds(millisUntilFinished)
                )
            }
        }
        finishCountDownTimer?.start()
    }

    private fun cancelFinishCountDownTimer() {
        finishCountDownTimer?.cancel()
    }

}