package com.slamtec.robot.deliver.fragment.setting


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.QueryCargosListener
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.agentservice.OperateBoxListener
import com.slamtec.robot.deliver.fragment.BaseFragment
import com.slamtec.robot.deliver.model.*
import com.slamtec.robot.deliver.utils.LogMgr
import kotlinx.android.synthetic.main.fragment_setting_cargo.*
import kotlinx.coroutines.*


class SettingCargoFragment : BaseFragment() {

    companion object {
        fun newInstance() =
            SettingCargoFragment()
    }

    private var cargoBoxes = listOf<CargoBox>()
    private var openCargoBoxes = mutableSetOf<CargoBox>()
    private var mDelayCheckJob: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_cargo, container, false)
    }

    override fun initView() {
        super.initView()
        queryCargos(this, object : QueryCargosListener {
            override fun onQuery(cargos: List<Cargo>) {
                    cargoBoxes = filterTakeoutBoxes(cargos)
                    refreshCargosView(cargoBoxes)
            }
        })
        setDelayCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelayCheckJob?.cancel()
    }


    fun getOpenBox(): Set<CargoBox> {
        return openCargoBoxes
    }


    private fun filterTakeoutBoxes(cargos: List<Cargo>): List<CargoBox> {
        val cargoBoxes = arrayListOf<CargoBox>()
        cargos.forEach {
            if (it.type == "TAKEOUT") {
                it.boxes.forEach { box ->
                    cargoBoxes.add(CargoBox(it.id, box))
                }
            }
        }
        return cargoBoxes
    }


    private fun setDelayCheck() {
        mDelayCheckJob?.cancel()
        mDelayCheckJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000 - 500)
            openCargoBoxes.forEach { cargoBox ->
                operateBox(
                    BoxCmd(
                        cargoBox.cargo_id,
                        cargoBox.box.id,
                        OperateType.CLOSE
                    ), object : OperateBoxListener {
                        override fun onSuccess(box: Cargo.Box) {
                            openCargoBoxes.remove(cargoBox)
                            LogMgr.d(">>>close box ${box?.id} success<<<")
                        }

                        override fun onFailed(box: FailedBox?) {
                            openCargoBoxes.remove(cargoBox)
                            LogMgr.d(">>>close box ${box?.box?.id} failed<<<")
                        }
                    })
            }
            updateViewType(RobotViewType.HOME)
        }
    }


    private fun refreshCargosView(cargoBoxes: List<CargoBox>) {
        if (cargoBoxes.isNotEmpty()) {
            val box1 = cargoBoxes[0]
            bv_cargo_open1?.visibility = View.VISIBLE
            bv_cargo_open2?.visibility = View.GONE
            bv_cargo_open1?.setLocation(box1.box.id)
            var boxState1 = when (StockStatus.valueOf(box1.box.stock_status)) {
                StockStatus.SEMIFULL, StockStatus.FULL -> {
                    BoxState.OPEN
                }
                StockStatus.EMPTY -> {
                    BoxState.OPEN
                }
            }
            if (BoxState.valueOf(box1.box.door_status) == BoxState.OPEN) {
                boxState1 = BoxState.CLOSED
            }
            if (Status.valueOf(box1.box.status) == Status.ERROR) {
                boxState1 = BoxState.CLOSED
            }
            bv_cargo_open1?.setBoxState(boxState1)
            bv_cargo_open1?.setOnClickListener {
                bv_cargo_open1?.isClickable = false
                if (bv_cargo_open1?.mBoxState == BoxState.OPEN) {
                    openCargoBoxes.add(box1)
                    bv_cargo_open1?.setBoxState(BoxState.OPENING)
                    operateBox(
                        BoxCmd(
                            box1.cargo_id,
                            box1.box.id,
                            OperateType.OPEN
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                bv_cargo_open1?.setBoxState(BoxState.CLOSED)
                                bv_cargo_open1?.isClickable = true
                                LogMgr.d(">>>open box ${box?.id} success<<<")
                            }

                            override fun onFailed(box: FailedBox?) {
                                LogMgr.d(">>>open box ${box?.box?.id} failed<<<")
                                openCargoBoxes.remove(box1)
                                bv_cargo_open1?.setBoxState(BoxState.CLOSED)
                            }
                        })
                }
                if (bv_cargo_open1?.mBoxState == BoxState.CLOSED) {
                    bv_cargo_open1?.setBoxState(BoxState.CLOSING)
                    operateBox(
                        BoxCmd(
                            box1.cargo_id,
                            box1.box.id,
                            OperateType.CLOSE
                        ), object : OperateBoxListener {
                            override fun onSuccess(box: Cargo.Box) {
                                bv_cargo_open1?.isClickable = true
                                openCargoBoxes.remove(box1)
                                if (StockStatus.valueOf(box.stock_status) == StockStatus.EMPTY) {
                                    bv_cargo_open1?.setBoxState(BoxState.OPEN)
                                } else {
                                    bv_cargo_open1?.setBoxState(BoxState.OPEN)
                                }
                                LogMgr.d(">>>close box ${box?.id} success<<<")
                            }

                            override fun onFailed(box: FailedBox?) {
                                bv_cargo_open1?.isClickable = true
                                openCargoBoxes.remove(box1)
                                LogMgr.d(">>>close box ${box?.box?.id} failed<<<")
                                bv_cargo_open1?.setBoxState(BoxState.OPEN)
                            }
                        })
                }
            }
            if (cargoBoxes.size > 1) {
                val box2 = cargoBoxes[1]
                bv_cargo_open2?.visibility = View.VISIBLE
                bv_cargo_open2?.setLocation(box2.box.id)
                var boxState2 = when (StockStatus.valueOf(box2.box.stock_status)) {
                    StockStatus.SEMIFULL, StockStatus.FULL -> {
                        BoxState.OPEN
                    }
                    StockStatus.EMPTY -> {
                        BoxState.OPEN
                    }
                }
                if (BoxState.valueOf(box2.box.door_status) == BoxState.OPEN) {
                    boxState2 = BoxState.CLOSED
                }
                if (Status.valueOf(box2.box.status) == Status.ERROR) {
                    boxState2 = BoxState.CLOSED
                }
                bv_cargo_open2?.setBoxState(boxState2)
                bv_cargo_open2?.setOnClickListener {
                    bv_cargo_open2?.isClickable = false
                    if (bv_cargo_open2?.mBoxState == BoxState.OPEN) {
                        openCargoBoxes.add(box2)
                        bv_cargo_open2?.setBoxState(BoxState.OPENING)
                        operateBox(
                            BoxCmd(
                                box2.cargo_id,
                                box2.box.id,
                                OperateType.OPEN
                            ), object : OperateBoxListener {
                                override fun onSuccess(box: Cargo.Box) {
                                    bv_cargo_open2?.setBoxState(BoxState.CLOSED)
                                    bv_cargo_open2?.isClickable = true
                                    LogMgr.d(">>>open box ${box?.id} success<<<")
                                }

                                override fun onFailed(box: FailedBox?) {
                                    LogMgr.d(">>>open box ${box?.box?.id} failed<<<")
                                    openCargoBoxes.remove(box2)
                                    bv_cargo_open2?.setBoxState(BoxState.CLOSED)
                                    bv_cargo_open2?.isClickable = true
                                }
                            })
                    }
                    if (bv_cargo_open2?.mBoxState == BoxState.CLOSED) {
                        bv_cargo_open2?.setBoxState(BoxState.CLOSING)
                        operateBox(
                            BoxCmd(
                                box2.cargo_id,
                                box2.box.id,
                                OperateType.CLOSE
                            ), object : OperateBoxListener {
                                override fun onSuccess(box: Cargo.Box) {
                                    bv_cargo_open2?.isClickable = true
                                    openCargoBoxes.remove(box2)
                                    if (StockStatus.valueOf(box.stock_status) == StockStatus.EMPTY) {
                                        bv_cargo_open2?.setBoxState(BoxState.OPEN)
                                    } else {
                                        bv_cargo_open2?.setBoxState(BoxState.OPEN)
                                        bv_cargo_open2?.isClickable = true
                                    }
                                    LogMgr.d(">>>close box ${box?.id} success<<<")
                                }

                                override fun onFailed(box: FailedBox?) {
                                    bv_cargo_open2?.isClickable = true
                                    openCargoBoxes.remove(box2)
                                    LogMgr.d(">>>close box ${box?.box?.id} failed<<<")
                                    bv_cargo_open2?.setBoxState(BoxState.OPEN)
                                }
                            })
                    }
                }
            }

        }

    }


}