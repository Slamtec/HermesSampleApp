package com.slamtec.robot.deliver.fragment

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.slamtec.robot.deliver.DeliveryStage
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.customview.DialogUtils
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.DateUtils
import com.slamtec.robot.deliver.utils.LogMgr

import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.*

class SendFragment : BaseFragment() {
    private var mWaitTipDialog: Dialog? = null
    private var mRetrieveTipDialog: Dialog? = null
    private var mDelayJob: Job? = null
    private var failedCount = 0
    private var countDownTimer: CountDownTimer? = null
    private var countDown = 5 * 60 * 1000
    private var mActionTimeoutJob: Job? = null
    private var mDialogTimeoutJob: Job? = null
    private var mWaitiDialogTimeoutJob: Job? = null

    enum class ViewState {
        SEND, SYNC, FAILED, CLOSING
    }

    companion object {
        fun newInstance() = SendFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_send, container, false)
    }

    override fun initView() {
        super.initView()
        tv_send_back.setOnClickListener {
            switchView(ViewState.CLOSING)
            val cargoBox = mRobotViewModel.openBox
            cargoBox?.let {
                operateBox(BoxCmd(cargoBox.cargo_id, cargoBox.box.id, OperateType.CLOSE),
                    object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            updateViewType(RobotViewType.INPUT)
                        }

                        override fun onFailed(box: FailedBox?) {
                            updateViewType(RobotViewType.INPUT)
                        }

                    })
            }

        }
        tv_put_package.text = getString(R.string.put_package, mRobotViewModel.inputNumber)
        tv_send.text = getString(R.string.distribute)

        tv_send.setOnClickListener {
            mRobotViewModel.isTimeoutCheck = false
            tv_send_back.visibility = View.GONE
            tv_send?.isClickable = false
            tv_send_second_order.isClickable = false
            updateViewType(RobotViewType.CLOSE)
        }
        if (mRobotViewModel.openBox!!.box.door_status == BoxState.CLOSED.toString()) {
            mRobotViewModel.isTimeoutCheck = false
            syncOrder()
            switchView(ViewState.SYNC)
        }
        tv_send_second_order.setOnClickListener {
            tv_send_second_order.isClickable = false
            mRobotViewModel.isCreateNextOrder = true
            updateViewType(RobotViewType.CLOSE)
        }
        if (mRobotViewModel.idleBoxes.size > 1) {
            tv_send_second_order?.visibility = View.VISIBLE
        } else {
            tv_send_second_order?.visibility = View.GONE
            LogMgr.e("no more empty cargos")
        }
        mRobotViewModel.isCreateNextOrder = false
        if (mRobotViewModel.isTimeoutCheck) setSendTimeoutCheck()
    }


    override fun onDestroy() {
        super.onDestroy()
        mDelayJob?.cancel()
        countDownTimer?.cancel()
        dismissWaitTipDialog()
        cancelActionTimeoutJob()
        cancelDialogTimeoutJob()
        dismissRetrieveTipDialog()
    }


    private fun syncOrder() {
        LogMgr.i(">>>create order success<<<")
        tv_send?.text = getText(R.string.ready_send)
        setDelayCheck()
        enableTask()
        mRobotViewModel.requestOrderList.clear()
    }

    private fun showWaitTipDialog() {
        mWaitTipDialog = DialogUtils.createTipDialog(
            context!!,
            getString(R.string.create_order_tip),
            View.OnClickListener {
                dismissWaitTipDialog()
                cancelWaitDialogTimeoutJob()
                updateViewType(RobotViewType.HOME)
            })
        if (mWaitTipDialog != null) {
            mWaitTipDialog!!.show()
            fullScreenImmersive(mWaitTipDialog!!.window!!.decorView)
        }
        setWaitDialogTimeoutCheck()
    }

    private fun setSendTimeoutCheck() {
        countDownTimer?.cancel()
        val untilFinished = mRobotViewModel.orderUnFinishCountDown * 1000
        countDownTimer = object : CountDownTimer(untilFinished, 100) {
            override fun onFinish() {
                mRobotViewModel.isTimeoutCheck = false
                tv_send_back?.visibility = View.GONE
                tv_send?.isClickable = false
                tv_send_second_order?.isClickable = false
                updateViewType(RobotViewType.CLOSE)
            }

            override fun onTick(millisUntilFinished: Long) {
                mRobotViewModel.orderUnFinishCountDown = millisUntilFinished / 1000
                val p = 100 * (countDown - millisUntilFinished) / countDown
                sv_send_sandglass.setProgress(p.toInt(), 100)
                tv_send_sandglass.text = DateUtils.formatDate(millisUntilFinished)
            }
        }
        countDownTimer?.start()
    }

    private fun setDelayCheck() {
        mDelayJob?.cancel()
        mDelayJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5000)
            if (mRobotViewModel.deliveryStage.value != DeliveryStage.ON_DELIVERING) {
                showWaitTipDialog()
            }
        }
    }


    private fun dismissWaitTipDialog() {
        mWaitTipDialog?.dismiss()
        var view = activity?.window?.decorView
        view?.let {
            fullScreenImmersive(view)
        }
    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.SEND -> {
                rl_send.visibility = View.VISIBLE
                rl_send_failed.visibility = View.GONE
                rl_sync_order.visibility = View.GONE
                rl_send_closing.visibility = View.GONE
            }
            ViewState.SYNC -> {
                rl_send.visibility = View.GONE
                rl_send_failed.visibility = View.GONE
                rl_sync_order.visibility = View.VISIBLE
                rl_send_closing.visibility = View.GONE
            }

            ViewState.CLOSING -> {
                rl_send.visibility = View.GONE
                rl_send_failed.visibility = View.GONE
                rl_sync_order.visibility = View.GONE
                rl_send_closing.visibility = View.VISIBLE
            }
            else -> {
                rl_sync_order?.visibility = View.GONE
                rl_send?.visibility = View.GONE
                rl_send_closing.visibility = View.GONE
                rl_send_failed?.visibility = View.VISIBLE
                val orderList = mRobotViewModel.requestOrderList
                setActionTimeoutCheck()
                refreshView(orderList)
            }
        }
    }

    private fun refreshView(orders: List<Order>) {
        failedCount = 1
        bv_send_retrieve1?.visibility = View.VISIBLE
        bv_send_retrieve2.visibility = View.GONE
        bv_send_retrieve1?.setLocation(orders[0].location.poi_name)
        bv_send_retrieve1?.setBoxState(BoxState.OPEN)
        bv_send_retrieve1?.setOnClickListener {
            bv_send_retrieve1?.isClickable = false
            if (bv_send_retrieve1?.mBoxState == BoxState.OPEN) {
                bv_send_retrieve1?.setBoxState(BoxState.OPENING)
                operateBox(
                    BoxCmd(
                        orders[0].cargos[0].cargo_id,
                        orders[0].cargos[0].boxes[0],
                        OperateType.OPEN
                    ), object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            bv_send_retrieve1?.setBoxState(BoxState.CLOSED)
                            bv_send_retrieve1?.isClickable = true
                        }

                        override fun onFailed(box: FailedBox?) {
                            bv_send_retrieve1?.setBoxState(BoxState.ERROR)
                            failedCount--
                            checkIsClean()
                        }
                    })
            }
            if (bv_send_retrieve1?.mBoxState == BoxState.CLOSED) {
                bv_send_retrieve1?.setBoxState(BoxState.CLOSING)
                operateBox(
                    BoxCmd(
                        orders[0].cargos[0].cargo_id,
                        orders[0].cargos[0].boxes[0],
                        OperateType.CLOSE
                    ), object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            bv_send_retrieve1?.setBoxState(BoxState.DONE)
                            failedCount--
                            box?.let {
                                if (StockStatus.valueOf(box.stock_status) != StockStatus.EMPTY) {
                                    bv_send_retrieve1?.setBoxState(BoxState.OPEN)
                                    bv_send_retrieve1?.isClickable = true
                                    failedCount++
                                }
                            }
                            checkIsClean()
                        }

                        override fun onFailed(box: FailedBox?) {
                            bv_send_retrieve1?.setBoxState(BoxState.ERROR)
                            failedCount--
                            checkIsClean()
                        }
                    })
            }
            checkIsClean()
        }
        if (orders.size > 1) {
            failedCount = 2
            bv_send_retrieve2?.visibility = View.VISIBLE
            bv_send_retrieve2?.setLocation(orders[1].location.poi_name)
            bv_send_retrieve2?.setBoxState(BoxState.OPEN)
            bv_send_retrieve2?.setOnClickListener {
                bv_send_retrieve2?.isClickable = false
                if (bv_send_retrieve2?.mBoxState == BoxState.OPEN) {
                    bv_send_retrieve2?.setBoxState(BoxState.OPENING)
                    operateBox(
                        BoxCmd(
                            orders[1].cargos[0].cargo_id,
                            orders[1].cargos[0].boxes[0],
                            OperateType.OPEN
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                bv_send_retrieve2?.setBoxState(BoxState.CLOSED)
                                bv_send_retrieve2?.isClickable = true
                            }

                            override fun onFailed(box: FailedBox?) {
                                bv_send_retrieve2?.setBoxState(BoxState.ERROR)
                                failedCount--
                                checkIsClean()
                            }
                        })
                }
                if (bv_send_retrieve2?.mBoxState == BoxState.CLOSED) {
                    bv_send_retrieve2?.setBoxState(BoxState.CLOSING)
                    operateBox(
                        BoxCmd(
                            orders[1].cargos[0].cargo_id,
                            orders[1].cargos[0].boxes[0],
                            OperateType.CLOSE
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                failedCount--
                                bv_send_retrieve2?.setBoxState(BoxState.DONE)
                                box?.let {
                                    if (StockStatus.valueOf(box.stock_status) != StockStatus.EMPTY) {
                                        bv_send_retrieve2?.setBoxState(BoxState.OPEN)
                                        bv_send_retrieve2?.isClickable = true
                                    }
                                }
                                checkIsClean()
                            }

                            override fun onFailed(box: FailedBox?) {
                                failedCount--
                                bv_send_retrieve2?.setBoxState(BoxState.ERROR)
                                checkIsClean()
                            }
                        })
                }
                checkIsClean()
            }
        }
    }

    private fun checkIsClean() {
        if (failedCount < 1) {
            mRobotViewModel.requestOrderList.clear()
            if (isAdded) {
                showRetrieveTipDialog(getString(R.string.retrieve_success))
                cancelActionTimeoutJob()
                setDialogTimeoutCheck()
            }
        }
    }

    private fun showRetrieveTipDialog(tip: String) {
        mRetrieveTipDialog = DialogUtils.createTipDialog(
            context!!,
            tip,
            getString(R.string.back_homepage),
            View.OnClickListener {
                dismissRetrieveTipDialog()
                cancelDialogTimeoutJob()
                updateViewType(RobotViewType.HOME)
            })
        if (mRetrieveTipDialog != null) {
            mRetrieveTipDialog!!.show()
            fullScreenImmersive(mRetrieveTipDialog!!.window!!.decorView)
        }
    }

    private fun dismissRetrieveTipDialog() {
        mRetrieveTipDialog?.dismiss()
        var view = activity?.window?.decorView
        view?.let {
            fullScreenImmersive(view)
        }
    }

    private fun setActionTimeoutCheck() {
        mActionTimeoutJob?.cancel()
        mActionTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            val orders = mRobotViewModel.requestOrderList
            orders.forEach {
                operateBox(BoxCmd(it.cargos[0].cargo_id, it.cargos[0].boxes[0], OperateType.CLOSE), object : OperateBoxListener {
                    override fun onSuccess(box: Cargo.Box) {

                    }

                    override fun onFailed(box: FailedBox?) {

                    }

                })
            }
            mRobotViewModel.requestOrderList.clear()
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
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelDialogTimeoutJob() {
        mDialogTimeoutJob?.cancel()
    }

    private fun setWaitDialogTimeoutCheck() {
        mWaitiDialogTimeoutJob?.cancel()
        mWaitiDialogTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(2 * 60 * 1000)
            dismissWaitTipDialog()
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelWaitDialogTimeoutJob() {
        mWaitiDialogTimeoutJob?.cancel()
    }

}