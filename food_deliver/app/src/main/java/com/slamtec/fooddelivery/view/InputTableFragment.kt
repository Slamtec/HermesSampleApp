package com.slamtec.fooddelivery.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.constants.Constants
import com.slamtec.fooddelivery.model.PlateInfo
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.DialogUtils
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.SoundPoolUtil
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_input_table.*
import kotlinx.android.synthetic.main.layout_robot.*

/**
 * File   : InputTableFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/10 6:42 PM
 */
class InputTableFragment : BaseFragment(), TableViewAdapter.OnItemClickListener {

    private var selectedPlate: PlateInfo? = null
    private var mBackToHomedDialog: Dialog? = null
    private var adapter: TableViewAdapter? = null
    private val mHandler = Handler {
        when (it.what) {
            0 -> {
                updateViewType(ViewType.DELIVERING)
            }
        }
        start_move.isEnabled = true
        start_move.isClickable = true
        false
    }

    companion object {
        fun newInstance() = InputTableFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_table, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        queryRobotHealth()
        val adapter = TableViewAdapter(context!!, mViewModel.mRoomList)
        adapter.setOnItemClickListener(this)
        /*mViewModel.mRoomList.add("dfdf")
        mViewModel.mRoomList.add("aaa")
        mViewModel.mRoomList.add("dddddd")
        mViewModel.mRoomList.add("djkfjk")*/
        rv_tables.adapter = adapter
        rv_tables.layoutManager = GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
        tv_1.setOnClickListener {
            selectPlate1()
        }
        tv_2.setOnClickListener {
            if (selectedPlate?.plateName == mViewModel.plate2.plateName) {
                LogMgr.d("重复点击选择！")
                return@setOnClickListener
            }
            adapter.clearPosition()
            //待放入菜品状态，点击即变为选中状态
            selectedPlate = mViewModel.plate2
            changeClickAfterStatus(mViewModel.plate2)
            setOnClickPlateTextView(mViewModel.plate2, tv_2, tv_1, tv_3, tv_4, iv_close_2)
            tv_choose.text = "请选择${selectedPlate?.plateName}餐桌号"
        }
        tv_3.setOnClickListener {
            if (selectedPlate?.plateName == mViewModel.plate3.plateName) {
                LogMgr.d("重复点击选择！")
                return@setOnClickListener
            }
            adapter.clearPosition()
            changeClickAfterStatus(mViewModel.plate3)
            //待放入菜品状态，点击即变为选中状态
            selectedPlate = mViewModel.plate3
            setOnClickPlateTextView(mViewModel.plate3, tv_3, tv_2, tv_1, tv_4, iv_close_3)
            tv_choose.text = "请选择${selectedPlate?.plateName}餐桌号"
        }
        tv_4.setOnClickListener {
            if (selectedPlate?.plateName == mViewModel.plate4.plateName) {
                LogMgr.d("重复点击选择！")
                return@setOnClickListener
            }
            adapter.clearPosition()
            selectedPlate = mViewModel.plate4
            changeClickAfterStatus(mViewModel.plate4)
            //待放入菜品状态，点击即变为选中状态
            setOnClickPlateTextView(mViewModel.plate4, tv_4, tv_2, tv_1, tv_3, iv_close_4)
//            tv_choose.text = "请选择${selectedPlate?.plateName}餐桌号"
        }
        setPlateText(mViewModel.plate1, tv_1, iv_close_1)
        setPlateText(mViewModel.plate2, tv_2, iv_close_2)
        setPlateText(mViewModel.plate3, tv_3, iv_close_3)
        setPlateText(mViewModel.plate4, tv_4, iv_close_4)
        start_move.setOnClickListener {
            start_move.isEnabled = false
            start_move.isClickable = false

            val checkHasTask = plates.find {
                it.selectedTable != "null" && (it.status == Constants.CHOSEN_TABLE
                        || it.status == Constants.DELIVERING_FOOD_TIME_OUT) || it.status == Constants.TAKEN_FOOD_TIME_OUT
            }
            LogMgr.d("start move now! : $checkHasTask")
            if (checkHasTask != null) {

                for (p in plates) {
                    if (p.selectedTable != "null" && (p.status == Constants.TAKEN_FOOD_TIME_OUT
                                    || p.status == Constants.DELIVERING_FOOD_TIME_OUT)) {
                        p.status = Constants.CHOSEN_TABLE
                    }
                }

                SoundPoolUtil.getInstance(context!!).playSoundUnfinished(R.raw.start_move)
                ll_btn.visibility = View.GONE
                cl_execute_task.visibility = View.VISIBLE
                Glide.with(context!!).load(R.drawable.tasks).into(iv_execute_task)
                mViewModel.deliveringPlate = checkHasTask
                mHandler.sendEmptyMessageDelayed(0, 1000)
            } else {
                mHandler.sendEmptyMessageDelayed(1, 500)
                ToastTool.showWarningToast(context!!, "请先选择餐桌号", 0, 30)
            }
        }
        ll_setting.setOnClickListener {
            updateViewType(ViewType.SETTING)
        }
        ll_back.setOnClickListener {
            showGoBackHomeDialog()
        }
        selectPlate1()
        iv_close_1.setOnClickListener {
            mViewModel.plate1.status = Constants.READY_PUT_FOOD
            mViewModel.plate1.selectedTable = "null"
            tv_1.text = mViewModel.plate1.plateName + " - 待放置菜品"
            iv_close_1.visibility = View.GONE
            adapter.clearPosition()
        }
        iv_close_2.setOnClickListener {
            mViewModel.plate2.status = Constants.READY_PUT_FOOD
            mViewModel.plate2.selectedTable = "null"
            tv_2.text = mViewModel.plate2.plateName + " - 待放置菜品"
            iv_close_2.visibility = View.GONE
            adapter.clearPosition()
        }
        iv_close_3.setOnClickListener {
            mViewModel.plate3.status = Constants.READY_PUT_FOOD
            mViewModel.plate3.selectedTable = "null"
            tv_3.text = mViewModel.plate3.plateName + " - 待放置菜品"
            iv_close_3.visibility = View.GONE
            adapter.clearPosition()
        }
        iv_close_4.setOnClickListener {
            mViewModel.plate4.status = Constants.READY_PUT_FOOD
            mViewModel.plate4.selectedTable = "null"
            tv_4.text = mViewModel.plate4.plateName + " - 待放置菜品"
            iv_close_4.visibility = View.GONE
            adapter.clearPosition()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectPlate1() {
        if (selectedPlate?.plateName == mViewModel.plate1.plateName) {
            LogMgr.d("重复点击选择！")
            return
        }
        adapter?.clearPosition()
        selectedPlate = mViewModel.plate1
        changeClickAfterStatus(mViewModel.plate1)
        //待放入菜品状态，点击即变为选中状态
        setOnClickPlateTextView(mViewModel.plate1, tv_1, tv_2, tv_3, tv_4, iv_close_1)
        tv_choose.text = "请选择${selectedPlate?.plateName}餐桌号"
    }

    fun changeClickAfterStatus(plateInfo: PlateInfo) {
        when (plateInfo.status) {
            Constants.READY_PUT_FOOD -> {
                Constants.CHOOSE_TABLE
            }
        }
    }

    private fun showGoBackHomeDialog() {
        if (mBackToHomedDialog == null) {
            mBackToHomedDialog = DialogUtils.createDoubleBtnDialog(
                    context,
                    "您当前设置了送餐任务，点击确定\n将不保存当前信息",
                    View.OnClickListener {
                        if (it.id == R.id.confirm) {
                            mViewModel.initPlate()
                            updateViewType(ViewType.HOME)
                        } else {
                            dismissBackHomeDialog()
                        }
                    })
        }
        if (mBackToHomedDialog != null && !mBackToHomedDialog!!.isShowing) {
            mBackToHomedDialog!!.show()
        }
    }

    private fun dismissBackHomeDialog() {
        mBackToHomedDialog?.dismiss()
    }

    private fun setOnClickPlateTextView(plateInfo: PlateInfo, tv: TextView, tv1: TextView, tv2: TextView, tv3: TextView, iv: ImageView) {
        tv.setBackgroundResource(R.drawable.robot_tray_l02_selected)
        normalPlateTextView(tv1, tv2, tv3)
        setPlateText(plateInfo, tv, iv)
    }

    @SuppressLint("SetTextI18n")
    fun setPlateText(plateInfo: PlateInfo, tv: TextView, iv: ImageView) {
        when (plateInfo.status) {
            0 -> {
                tv.text = plateInfo.plateName + " - 待放置菜品"
                iv.visibility = View.GONE
            }
            1 -> {
                tv.text = plateInfo.plateName + " - 选择餐桌号"
                iv.visibility = View.GONE
            }
            2, 3, 5, 6 -> {
                tv.text = plateInfo.plateName + " - ${plateInfo.selectedTable}待取餐"
                iv.visibility = View.VISIBLE
            }
            4 -> {
                tv.text = plateInfo.plateName + " - 已取餐"
                iv.visibility = View.GONE
            }
        }
    }

    private fun normalPlateTextView(vararg tv: TextView) {
        for (view in tv) {
            view.setBackgroundResource(R.drawable.robot_tray_l02_unselected)
        }
    }

    /**
     * 点击右边请选择餐桌
     */
    override fun onItemClick(v: View, position: Int, poiName: String) {
        if (selectedPlate == null) {
            ToastTool.showWarningToast(context!!, "尚无配送任务，请放置菜品选择桌号", 0, 30)
            return
        }
        selectedPlate?.selectedTable = poiName
        selectedPlate?.status = Constants.CHOSEN_TABLE
        LogMgr.d("selected plate: ${selectedPlate.toString()}")
        when (selectedPlate?.plateName) {
            "1L" -> {
                setPlateText(selectedPlate!!, tv_1, iv_close_1)
                mViewModel.plate1.status = Constants.CHOSEN_TABLE
                mViewModel.plate1.selectedTable = poiName
                iv_close_1.visibility = View.VISIBLE
            }
            "2L" -> {
                setPlateText(selectedPlate!!, tv_2, iv_close_2)
                mViewModel.plate2.status = Constants.CHOSEN_TABLE
                mViewModel.plate2.selectedTable = poiName
                iv_close_2.visibility = View.VISIBLE
            }
            "3L" -> {
                setPlateText(selectedPlate!!, tv_3, iv_close_3)
                mViewModel.plate3.status = Constants.CHOSEN_TABLE
                mViewModel.plate3.selectedTable = poiName
                iv_close_3.visibility = View.VISIBLE
            }
            "4L" -> {
                setPlateText(selectedPlate!!, tv_4, iv_close_4)
                mViewModel.plate4.status = Constants.CHOSEN_TABLE
                mViewModel.plate4.selectedTable = poiName
                iv_close_4.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBackToHomedDialog?.dismiss()
        mHandler.removeCallbacksAndMessages(null)
    }

}