package com.slamtec.robot.deliver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.RobotViewType
import kotlinx.android.synthetic.main.fragment_error.*

class ErrorFragment : BaseFragment() {
    companion object {
        fun newInstance() = ErrorFragment()
    }

    enum class ViewState {
        EMPTY, RETRIEVE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun initView() {
        super.initView()
        tv_error_retrieve?.setOnClickListener {
            mRobotViewModel.updateViewType(RobotViewType.SETTING)
        }

        val deviceError = mRobotViewModel.deviceError
        deviceError?.let {
            val errors = deviceError.error.filter { it.error_code != 255 }
            val maxLevelError = errors.sortedByDescending { it.error_code }[0]
            tv_error_retrieve_code?.text = getString(R.string.device_error, maxLevelError.error_code.toString())
            tv_error_home_code?.text = getString(R.string.device_error, maxLevelError.error_code.toString())
        } ?: updateViewType(RobotViewType.HOME)
        switchView(ViewState.RETRIEVE)

    }

    private fun switchView(viewState: ViewState) {
        when (viewState) {
            ViewState.RETRIEVE -> {
                rl_error_retrieve.visibility = View.VISIBLE
                rl_error_empty.visibility = View.GONE
            }
            else -> {
                rl_error_retrieve.visibility = View.GONE
                rl_error_empty.visibility = View.VISIBLE
            }
        }
    }
}