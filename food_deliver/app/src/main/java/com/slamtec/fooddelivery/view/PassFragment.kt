package com.slamtec.fooddelivery.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slamtec.fooddelivery.model.PoiType

/**
 * File   : PassFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/11 11:15 AM
 */
class PassFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun initView() {
        super.initView()
        queryRobotHealth()
    }
}