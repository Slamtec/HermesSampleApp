package com.slamtec.robot.deliver.fragment.setting

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.slamtec.robot.deliver.DeliveryStage
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.customview.DialogUtils
import com.slamtec.robot.deliver.fragment.BaseFragment
import com.slamtec.robot.deliver.model.*
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : BaseFragment() {

    companion object {
        fun newInstance() =
            SettingFragment()
    }

    private var mTipDialog: Dialog? = null
    private var mCargoFragment: SettingCargoFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun initView() {
        super.initView()
        disableTask()
        replaceFragment(SettingConfigFragment.newInstance(), R.id.fl_setting_fragment)
        smv_setting_menu.setOnSelectListener {
            onSelectConfig {
                smv_setting_menu.selectConfig()
                replaceFragment(SettingConfigFragment.newInstance(), R.id.fl_setting_fragment)
            }
            onSelectCargo {
                smv_setting_menu.selectCargo()
                mCargoFragment = SettingCargoFragment.newInstance()
                replaceFragment(mCargoFragment!!, R.id.fl_setting_fragment)
            }
        }
        ll_setting_back.setOnClickListener {
            if (mRobotViewModel.deliveryStage.value == DeliveryStage.DEVICE_ERROR) {
                closeOpenBox()
                updateViewType(RobotViewType.ERROR)
            } else {
                updateViewType(RobotViewType.HOME)
            }
        }
    }



    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
        beginTransaction().func().commit()

    private fun replaceFragment(fragment: Fragment, fragmentId: Int) =
        parentFragmentManager.inTransaction { replace(fragmentId, fragment) }

    private fun dismissTipDialog() {
        mTipDialog?.dismiss()
        var view = activity?.window?.decorView
        view?.let {
            fullScreenImmersive(view)
        }
    }

}