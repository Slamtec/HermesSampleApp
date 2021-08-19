package com.slamtec.robot.deliver.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.customview.DialogUtils
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_retrieve.*
import kotlinx.coroutines.*


class RetrieveFragment : BaseFragment() {
    private var mTipDialog: Dialog? = null
    private var mActionTimeoutJob: Job? = null
    private var mDialogTimeoutJob: Job? = null
    private var failedCount = 0
    private var errorCount = 0

    enum class ViewState {
        RETRIEVE, EMPTY
    }

    companion object {
        fun newInstance() = RetrieveFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_retrieve, container, false)
    }


    override fun initView() {
        super.initView()
        val failedTasks = mRobotViewModel.taskInfo?.task?.failed_tasks
        if (failedTasks != null && failedTasks.isNotEmpty()) {
            refreshView(failedTasks)
            setActionTimeoutCheck()
        } else {
            updateViewType(RobotViewType.ERROR)
        }
    }

    private fun refreshView(failedTasks: List<TaskInfo.Task.FailedTask>) {
        failedCount = 1
        bv_retrieve_retrieve1?.visibility = View.VISIBLE
        bv_retrieve_retrieve2?.visibility = View.GONE
        bv_retrieve_retrieve1?.setLocation(failedTasks[0].target)
        bv_retrieve_retrieve1?.setBoxState(BoxState.OPEN)
        bv_retrieve_retrieve1?.setOnClickListener {
            bv_retrieve_retrieve1?.isClickable = false
            if (bv_retrieve_retrieve1?.mBoxState == BoxState.OPEN) {
                bv_retrieve_retrieve1?.setBoxState(BoxState.OPENING)
                operateBox(
                    BoxCmd(
                        failedTasks[0].cargos[0].cargo_id,
                        failedTasks[0].cargos[0].boxes[0],
                        OperateType.OPEN
                    ), object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            bv_retrieve_retrieve1?.setBoxState(BoxState.CLOSED)
                            bv_retrieve_retrieve1?.isClickable = true
                            LogMgr.d(">>>retrieve open box ${box?.id} success<<<")
                            checkRetrieveResult()
                        }

                        override fun onFailed(box: FailedBox?) {
                            LogMgr.d(">>>retrieve open box failed<<<")
                            failedCount--
                            errorCount++
                            bv_retrieve_retrieve1?.setBoxState(BoxState.ERROR)
                            checkRetrieveResult()
                        }
                    })
            }
            if (bv_retrieve_retrieve1?.mBoxState == BoxState.CLOSED) {
                bv_retrieve_retrieve1?.setBoxState(BoxState.CLOSING)
                operateBox(
                    BoxCmd(
                        failedTasks[0].cargos[0].cargo_id,
                        failedTasks[0].cargos[0].boxes[0],
                        OperateType.CLOSE
                    ), object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            LogMgr.d(">>>retrieve close box ${box?.id} success<<<")
                            bv_retrieve_retrieve1?.setBoxState(BoxState.DONE)
                            failedCount--
                            box?.let {
                                if (StockStatus.valueOf(box.stock_status) != StockStatus.EMPTY) {
                                    bv_retrieve_retrieve1?.setBoxState(BoxState.OPEN)
                                    bv_retrieve_retrieve1?.isClickable = true
                                    failedCount++
                                }
                            }
                            checkRetrieveResult()
                        }

                        override fun onFailed(box: FailedBox?) {
                            bv_retrieve_retrieve1?.setBoxState(BoxState.ERROR)
                            LogMgr.d(">>>retrieve close box ${box?.box?.id} failed<<<")
                            failedCount--
                            checkRetrieveResult()
                        }
                    })
            }
        }
        if (failedTasks.size > 1) {
            failedCount = 2
            bv_retrieve_retrieve2?.visibility = View.VISIBLE
            bv_retrieve_retrieve2?.setLocation(failedTasks[1].target)
            bv_retrieve_retrieve2?.setBoxState(BoxState.OPEN)
            bv_retrieve_retrieve2?.setOnClickListener {
                bv_retrieve_retrieve2?.isClickable = false
                if (bv_retrieve_retrieve2?.mBoxState == BoxState.OPEN) {
                    bv_retrieve_retrieve2?.setBoxState(BoxState.OPENING)
                    operateBox(
                        BoxCmd(
                            failedTasks[1].cargos[0].cargo_id,
                            failedTasks[1].cargos[0].boxes[0],
                            OperateType.OPEN
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                LogMgr.d(">>>retrieve open box ${box?.id} success<<<")
                                bv_retrieve_retrieve2?.setBoxState(BoxState.CLOSED)
                                bv_retrieve_retrieve2?.isClickable = true
                                checkRetrieveResult()
                            }

                            override fun onFailed(box: FailedBox?) {
                                LogMgr.d(">>>retrieve open box failed<<<")
                                failedCount--
                                errorCount++
                                bv_retrieve_retrieve2?.setBoxState(BoxState.ERROR)
                                checkRetrieveResult()
                            }
                        })
                }
                if (bv_retrieve_retrieve2?.mBoxState == BoxState.CLOSED) {
                    bv_retrieve_retrieve2?.setBoxState(BoxState.CLOSING)
                    operateBox(
                        BoxCmd(
                            failedTasks[1].cargos[0].cargo_id,
                            failedTasks[1].cargos[0].boxes[0],
                            OperateType.CLOSE
                        ), object : OperateBoxListener {

                            override fun onSuccess(box: Cargo.Box) {
                                LogMgr.d(">>>retrieve close box ${box?.id} success<<<")
                                bv_retrieve_retrieve2?.setBoxState(BoxState.DONE)
                                failedCount--
                                box?.let {
                                    if (StockStatus.valueOf(box.stock_status) != StockStatus.EMPTY) {
                                        bv_retrieve_retrieve2?.setBoxState(BoxState.OPEN)
                                        bv_retrieve_retrieve2?.isClickable = true
                                        failedCount++
                                    }
                                }
                                checkRetrieveResult()
                            }

                            override fun onFailed(box: FailedBox?) {
                                bv_retrieve_retrieve2?.setBoxState(BoxState.ERROR)
                                LogMgr.d(">>>retrieve close box ${box?.box?.id} failed<<<")
                                failedCount--
                                checkRetrieveResult()
                            }
                        })
                }

            }
        }
    }

    private fun checkRetrieveResult() {
        if (failedCount < 1) {
            if (isAdded) {
                val tip =
                    if (errorCount > 0) getString(R.string.retrieve_failed) else getString(R.string.retrieve_success)
                val batteryPercentage = mRobotViewModel.powerStatus?.batteryPercentage ?: 0
                if (batteryPercentage <= 10) {
                    showTipDialog(tip, getString(R.string.back_charge), null)
                } else {
                    showTipDialog(tip, getString(R.string.back_homepage), getString(R.string.execute_task))
                }
                cancelActionTimeoutJob()
                setDialogTimeoutCheck()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissTipDialog()
        cancelDialogTimeoutJob()
        cancelActionTimeoutJob()
    }
    private fun showTipDialog(tip: String, choose1: String, choose2: String?) {
        mTipDialog = DialogUtils.createChooseDialog(
            context!!, tip,
            choose1, choose2,
            View.OnClickListener {
                if (it.id != R.id.tv_choose_1 || choose2 == null) {
                    enableTask()
                }
                dismissTipDialog()
                cancelDialogTimeoutJob()
                endPickup()
                updateViewType(RobotViewType.HOME)
            })
        if (mTipDialog != null) {
            mTipDialog!!.show()
            fullScreenImmersive(mTipDialog!!.window!!.decorView)
        }
    }


    private fun dismissTipDialog() {
        mTipDialog?.dismiss()
        var view = activity?.window?.decorView
        view?.let {
            fullScreenImmersive(view)
        }
    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.RETRIEVE -> {
                rl_retrieve.visibility = View.VISIBLE
                rl_retrieve_empty.visibility = View.GONE
            }
            ViewState.EMPTY -> {
                rl_retrieve_empty.visibility = View.VISIBLE
                rl_retrieve.visibility = View.GONE
            }
        }
    }

    private fun setActionTimeoutCheck() {
        mActionTimeoutJob?.cancel()
        mActionTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            val failedTasks = mRobotViewModel.taskInfo?.task?.failed_tasks
            if (failedTasks != null && failedTasks.isNotEmpty()) {
                failedTasks.forEach { failedTask ->
                    operateBox(
                        BoxCmd(
                            failedTask.cargos[0].cargo_id,
                            failedTask.cargos[0].boxes[0],
                            OperateType.CLOSE
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                            }

                            override fun onFailed(box: FailedBox?) {
                            }
                        })
                }
            }
            endPickup()
            val batteryPercentage = mRobotViewModel.powerStatus?.batteryPercentage ?: 0
            if (batteryPercentage <= 10) {
                enableTask()
            }
            updateViewType(RobotViewType.HOME)
        }
    }


    private fun cancelActionTimeoutJob() {
        mActionTimeoutJob?.cancel()
    }


    private fun setDialogTimeoutCheck() {
        mDialogTimeoutJob?.cancel()
        mDialogTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(2 * 60 * 1000)
            dismissTipDialog()
            endPickup()
            val batteryPercentage = mRobotViewModel.powerStatus?.batteryPercentage ?: 0
            if (batteryPercentage <= 10) {
                enableTask()
            }
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelDialogTimeoutJob() {
        mDialogTimeoutJob?.cancel()
    }


    private fun endPickup() {
        mRobotViewModel.endPickup().observe(this, Observer {
            LogMgr.i("end pickup response ${it.getOrNull()}")
        })
    }
}