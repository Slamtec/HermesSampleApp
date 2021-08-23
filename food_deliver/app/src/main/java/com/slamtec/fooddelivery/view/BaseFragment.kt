package com.slamtec.fooddelivery.view

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.slamtec.fooddelivery.model.FoodDeliverViewModel
import com.slamtec.fooddelivery.model.PlateInfo
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.DateUtils
import com.slamtec.fooddelivery.utils.LogMgr

/**
 * File   : BaseFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/10 6:42 PM
 */
abstract class BaseFragment : Fragment() {
    lateinit var mViewModel: FoodDeliverViewModel
    var countDownTimer: CountDownTimer? = null
    var plates = arrayListOf<PlateInfo>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    open fun initView() {
        mViewModel = (activity!! as MainActivity).getViewModel()
        startCountDownTimer(onSetCountDownTime())
        plates.add(mViewModel.plate1)
        plates.add(mViewModel.plate2)
        plates.add(mViewModel.plate3)
        plates.add(mViewModel.plate4)
    }

    fun queryRobotHealth() {
        mViewModel.robotHealth.observe(this, Observer {
            if (it != null && (it.hasFatal || it.hasError)) {
                updateViewType(ViewType.ERROR)
            }
        })
    }

    fun updateViewType(type: ViewType) {
        mViewModel.updateViewType(type)
    }

    fun backView() {
        mViewModel.backView()
    }

    fun startCountDownTimer(countDown: Int) {
        LogMgr.i("set count down $countDown")
        if (countDown > 0) {
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer((countDown * 1000).toLong(), 100) {
                override fun onFinish() {
                    onCountDownFinish()
                }

                override fun onTick(millisUntilFinished: Long) {
                    val p = 100 * (countDown - millisUntilFinished) / countDown
                    onCountDownTick(
                            millisUntilFinished,
                            p.toInt(),
                            DateUtils.formatToMS(millisUntilFinished)
                    )
                }
            }
            countDownTimer?.start()
        }
    }

    fun cancelCountDown() {
        countDownTimer?.cancel()
    }

    open fun onSetCountDownTime(): Int {
        return 0
    }

    open fun onCountDownFinish() {

    }

    open fun onCountDownTick(
            millisUntilFinished: Long,
            finishedPercent: Int,
            untilFinishedDate: String
    ) {

    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
            beginTransaction().func().commit()

    fun replaceFragment(fragment: Fragment, fragmentId: Int) =
            parentFragmentManager.inTransaction { replace(fragmentId, fragment) }

    fun removeFragment(fragment: Fragment) =
            parentFragmentManager.inTransaction { remove(fragment) }

    fun removeFragment(id: Int) {
        val fragment = parentFragmentManager.findFragmentById(id)
        fragment?.let {
            parentFragmentManager.inTransaction { remove(fragment) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelCountDown()
    }
}