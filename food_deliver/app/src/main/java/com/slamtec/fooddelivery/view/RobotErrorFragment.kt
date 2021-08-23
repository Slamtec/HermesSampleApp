package com.slamtec.fooddelivery.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.model.ViewType
import kotlinx.android.synthetic.main.fragment_robot_error.*

/**
 * File   : RobotErrorFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/13 10:24 AM
 */
class RobotErrorFragment : BaseFragment() {
    companion object {
        fun newInstance() = RobotErrorFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_robot_error, container, false)
    }

    override fun initView() {
        super.initView()
        cl_back.setOnClickListener {
            updateViewType(ViewType.HOME)
        }
        tv_i_know.setOnClickListener {
            updateViewType(ViewType.HOME)
        }
    }
}