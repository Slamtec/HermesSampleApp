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
import kotlinx.android.synthetic.main.fragment_box_close.*
import kotlinx.android.synthetic.main.fragment_create.*
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.*

class CreateFragment : BaseFragment() {
    private var mFailedDialog: Dialog? = null
    private var mEmptyBoxes = arrayListOf<CargoBox>()
    private var mActionTimeoutJob: Job? = null
    private var mTipTimeoutJob: Job? = null

    enum class ViewState {
        CREATING, RETRIEVE, FAILED, SECOND_FAILED
    }

    companion object {
        fun newInstance() = CreateFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun initView() {
        super.initView()
        switchView(ViewState.CREATING)
        createOrder()
        tv_create_order_failed_home.setOnClickListener {
            tv_box_close_failed_home.isClickable = false
            updateViewType(RobotViewType.HOME)
        }
        tv_create_order_failed_retry.setOnClickListener {
            tv_create_order_failed_retry.isClickable = false
            updateViewType(RobotViewType.INPUT)
        }
        tv_create_second_failed.setOnClickListener {
            updateViewType(RobotViewType.SEND)
        }
    }

    private fun createOrder() {
        val location = Location(mRobotViewModel.inputNumber!!)
        val orderCargo = OrderCargo(
            mRobotViewModel.openBox!!.cargo_id,
            arrayListOf(mRobotViewModel.openBox!!.box.id)
        )
        val order = Order(location, arrayListOf(orderCargo))
        mRobotViewModel.createTask(order).observe(this, Observer {
            val createResponse = it.getOrNull()
            LogMgr.i("create  $order response $createResponse")
            if (createResponse != null && createResponse.code() == 200) {
                mRobotViewModel.requestOrderList.add(order)
                if (mRobotViewModel.isCreateNextOrder) {
                    updateViewType(RobotViewType.INPUT)
                } else {
                    updateViewType(RobotViewType.SEND)
                }
            } else {
                //校验订单失败，先取出物品
                switchView(ViewState.RETRIEVE)
            }
        })
    }


    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.SECOND_FAILED -> {
                rl_create?.visibility = View.GONE
                rl_create_order_retrieve?.visibility = View.GONE
                rl_create_order_failed?.visibility = View.GONE
                rl_create_second_order_failed?.visibility = View.VISIBLE
            }
            ViewState.CREATING -> {
                rl_create?.visibility = View.VISIBLE
                rl_create_order_retrieve?.visibility = View.GONE
                rl_create_order_failed?.visibility = View.GONE
                rl_create_second_order_failed?.visibility = View.GONE
            }
            ViewState.RETRIEVE -> {
                rl_create?.visibility = View.GONE
                rl_create_order_retrieve?.visibility = View.VISIBLE
                rl_create_order_failed?.visibility = View.GONE
                rl_create_second_order_failed?.visibility = View.GONE
                bv_create_retrieve?.setBoxState(BoxState.OPEN)
                bv_create_retrieve?.setLocation(mRobotViewModel.inputNumber!!)
                val cargoBox = mRobotViewModel.openBox
                setActionTimeoutCheck()
                bv_create_retrieve?.setOnClickListener {
                    bv_create_retrieve?.isClickable = false
                    if (bv_create_retrieve?.mBoxState == BoxState.OPEN) {
                        bv_create_retrieve?.setBoxState(BoxState.OPENING)
                        operateBox(
                            BoxCmd(cargoBox!!.cargo_id, cargoBox.box.id, OperateType.OPEN),
                            object : OperateBoxListener {
                                override fun onSuccess(box: Cargo.Box) {
                                    bv_create_retrieve?.setBoxState(BoxState.CLOSED)
                                    bv_create_retrieve?.isClickable = true
                                }

                                override fun onFailed(box: FailedBox?) {
                                    bv_create_retrieve?.setBoxState(BoxState.ERROR)
                                    val viewState =
                                        if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_FAILED else ViewState.FAILED
                                    switchView(viewState)
                                }
                            })
                    }
                    if (bv_create_retrieve?.mBoxState == BoxState.CLOSED) {
                        bv_create_retrieve?.setBoxState(BoxState.CLOSING)
                        operateBox(
                            BoxCmd(cargoBox!!.cargo_id, cargoBox.box.id, OperateType.CLOSE),
                            object : OperateBoxListener {
                                override fun onSuccess(box: Cargo.Box) {
                                    bv_create_retrieve?.setBoxState(BoxState.DONE)
                                    val viewState =
                                        if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_FAILED else ViewState.FAILED
                                    box?.let {
                                        if (StockStatus.valueOf(box.stock_status) != StockStatus.EMPTY) {
                                            bv_create_retrieve?.setBoxState(BoxState.OPEN)
                                            bv_create_retrieve?.isClickable = true
                                        } else {
                                            switchView(viewState)
                                        }
                                    }
                                }

                                override fun onFailed(box: FailedBox?) {
                                    bv_create_retrieve?.setBoxState(BoxState.ERROR)
                                    val viewState =
                                        if (mRobotViewModel.requestOrderList.size > 0) ViewState.SECOND_FAILED else ViewState.FAILED
                                    switchView(viewState)
                                }

                            })
                    }
                }
            }
            else -> {
                cancelActionTimeoutJob()
                setTipTimeoutCheck()
                rl_create?.visibility = View.GONE
                rl_create_order_retrieve?.visibility = View.GONE
                rl_create_order_failed?.visibility = View.VISIBLE
                rl_create_second_order_failed?.visibility = View.GONE
                tv_create_order_failed_retry?.visibility =
                    if (mEmptyBoxes.size > 0) View.VISIBLE else View.GONE
                tv_create_order_failed_home?.setOnClickListener {
                    tv_create_order_failed_home?.isClickable = false
                    cancelTipTimeoutJob()
                    updateViewType(RobotViewType.HOME)
                }
                tv_create_order_failed_retry?.setOnClickListener {
                    tv_create_order_failed_retry?.isClickable = false
                    cancelTipTimeoutJob()
                    updateViewType(RobotViewType.INPUT)
                }
            }
        }
    }

    private fun showFailedDialog() {
        mFailedDialog = DialogUtils.createTipDialog(
            context!!,
            getString(R.string.order_failed_tip),
            View.OnClickListener {
                dismissFailedDialog()
                updateViewType(RobotViewType.INPUT)
            })
        if (mFailedDialog != null) {
            mFailedDialog!!.show()
            fullScreenImmersive(mFailedDialog!!.window!!.decorView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissFailedDialog()
        cancelActionTimeoutJob()
        cancelTipTimeoutJob()
    }

    private fun dismissFailedDialog() {
        mFailedDialog?.dismiss()
        var view = activity?.window?.decorView
        view?.let {
            fullScreenImmersive(view)
        }
    }

    private fun setActionTimeoutCheck() {
        mActionTimeoutJob?.cancel()
        mActionTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            val openBox = mRobotViewModel.openBox
            openBox?.let {
                operateBox(
                    BoxCmd(openBox.cargo_id, openBox.box.id, OperateType.CLOSE),
                    object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {

                        }

                        override fun onFailed(failedBox: FailedBox?) {

                        }
                    })

            }
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelActionTimeoutJob() {
        mActionTimeoutJob?.cancel()
    }

    private fun setTipTimeoutCheck() {
        mTipTimeoutJob?.cancel()
        mTipTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(2 * 60 * 1000)
            updateViewType(RobotViewType.HOME)
        }
    }

    private fun cancelTipTimeoutJob() {
        mTipTimeoutJob?.cancel()
    }
}