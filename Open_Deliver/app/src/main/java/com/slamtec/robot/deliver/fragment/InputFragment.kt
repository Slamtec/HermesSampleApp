package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.PoiType
import com.slamtec.robot.deliver.model.RobotViewType
import com.slamtec.robot.deliver.utils.DateUtils
import kotlinx.android.synthetic.main.fragment_input.*
import kotlinx.android.synthetic.main.fragment_input.nkv_number_keyboard

class InputFragment : BaseFragment() {
    companion object {
        fun newInstance() = InputFragment()
    }

    enum class ViewState {
        NOEMPTYBOX, INPUT
    }


    private var countDownTimer: CountDownTimer? = null
    private var countDown = 5 * 60 * 1000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun initView() {
        super.initView()
        if (mRobotViewModel.idleBoxes.isEmpty()) {
            switchView(ViewState.NOEMPTYBOX)
        }
        mRobotViewModel.isTimeoutCheck = true
        rnv_roomnumber?.setNumberLength(mRobotViewModel.mRoomNumberLength)
        nkv_number_keyboard.setOnClickListener {
            onClickDel {
                rnv_roomnumber.deleteNumber()
            }
            onClickConfirm {
                val inputRoomNum = rnv_roomnumber.getRoomNumber()
                if (mRobotViewModel.mRoomList.contains(inputRoomNum)) {
                    updateViewType(RobotViewType.OPEN)
                    mRobotViewModel.inputNumber = inputRoomNum
                } else {
                    showErrorView()
                }
            }
            onClickNumber {
                rnv_roomnumber.inputNumber(it)
                showTip()
            }
        }
        tv_homepage.setOnClickListener { updateViewType(RobotViewType.HOME) }
        countDownTimer = object : CountDownTimer(countDown.toLong(), 100) {
            override fun onFinish() {
                updateViewType(RobotViewType.HOME)
            }

            override fun onTick(millisUntilFinished: Long) {
                mRobotViewModel.orderUnFinishCountDown = millisUntilFinished / 1000
                val p = 100 * (countDown - millisUntilFinished) / countDown
                sv_input_sandglass.setProgress(p.toInt(), 100)
                tv_input_sandglass.text = DateUtils.formatDate(millisUntilFinished)
            }
        }
        countDownTimer?.start()
        mRobotViewModel.queryPois().observe(this, androidx.lifecycle.Observer { result ->
            val pois = result.getOrNull()
            pois?.let {
                val roomList = pois.filter { PoiType.ROOM == PoiType.valueOf(it.type) }
                val roomNameList = roomList.map { room -> room.poi_name }
                mRobotViewModel.mRoomList.clear()
                mRobotViewModel.mRoomList.addAll(roomNameList)
                mRobotViewModel.mRoomList.sortByDescending { it.length }
                if (mRobotViewModel.mRoomList.isNotEmpty()) {
                    mRobotViewModel.mRoomNumberLength = mRobotViewModel.mRoomList[0].length
                }
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun showErrorView() {
        rnv_roomnumber.clearNumber()
        val shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.animation_shake);
        rnv_roomnumber.startAnimation(shakeAnimation)
        tv_number_tip.setText(R.string.room_number_tip_error)
        tv_number_tip.setTextColor(ContextCompat.getColor(activity!!, R.color.text_red))
    }

    private fun showTip() {
        tv_number_tip.setText(R.string.room_number_tip)
        tv_number_tip.setTextColor(ContextCompat.getColor(context!!, R.color.white))
    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.NOEMPTYBOX -> {
                rl_input_no_box.visibility = View.VISIBLE
                rl_input.visibility = View.GONE
                tv_input_no_box.setOnClickListener {
                    tv_input_no_box.isClickable = false
                    updateViewType(RobotViewType.HOME)
                }
            }
            else -> {
                rl_input_no_box.visibility = View.VISIBLE
                rl_input.visibility = View.GONE
            }
        }
    }

}