package com.slamtec.robot.deliver.fragment.setting

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.fragment.BaseFragment
import com.slamtec.robot.deliver.utils.AppUtils
import com.tamsiree.rxkit.RxShellTool
import kotlinx.android.synthetic.main.fragment_setting_config.*


class SettingConfigFragment : BaseFragment() {
    private var count = 0

    companion object {
        fun newInstance() =
            SettingConfigFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting_config, container, false)
    }

    override fun initView() {
        super.initView()
        mRobotViewModel.queryConfigInfo().observe(this, Observer {
            val configInfoBean = it.getOrNull()
            tv_config_device_sn?.text = configInfoBean?.device_sn
            tv_config_firmware_version?.text = configInfoBean?.firmware_version
            tv_config_cloud_version?.text = configInfoBean?.cloud_version
        })
        tv_config_app_version?.text = AppUtils.getVersionName(context!!)
        tv_config_app_version?.setOnClickListener {
            count++
            if (count > 11) {
                activity?.finish()
            }
        }
    }
}