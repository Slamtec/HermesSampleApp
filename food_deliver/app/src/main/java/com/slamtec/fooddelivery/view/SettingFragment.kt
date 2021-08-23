package com.slamtec.fooddelivery.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.api.Repository
import com.slamtec.fooddelivery.constants.Constants
import com.slamtec.fooddelivery.model.ShutdownRequest
import com.slamtec.fooddelivery.model.SystemParameter
import com.slamtec.fooddelivery.model.ViewType
import com.slamtec.fooddelivery.utils.DialogUtils
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.ToastTool
import kotlinx.android.synthetic.main.fragment_input_table.*
import kotlinx.android.synthetic.main.fragment_setting.*

/**
 * File   : SettingFragment
 * Author : Qikun.Xiong
 * Date   : 2021/8/11 11:11 AM
 */
class SettingFragment : BasePlateFragment() {
    private var mBackToHomedDialog: Dialog? = null
    private var mGotoGetFoodDialog: Dialog? = null
    private var mShutDownDialog: Dialog? = null
    private var mDeviceInfoDialog: Dialog? = null
    private var mSetSpeedDialog: SetSpeedDialog? = null

    companion object {
        fun newInstance() =
                SettingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun initView() {
        super.initView()
        queryRobotHealth()
        ll_go_back_home.setOnClickListener {
            showGoBackHomeDialog()
        }
        ll_goto_get_food.setOnClickListener {
            mViewModel.refillPoi?.let {
                LogMgr.d("refillPoi:${mViewModel.refillPoi}")
                showGotoGetFoodDialog()
            } ?: let {
                ToastTool.showWarningToast(context!!, "当前地图未设置取餐点，请先设置取餐点", 0, 30)
            }
        }
        ll_shut_down.setOnClickListener {
            showShutDownDialog()
        }
        ll_device_info.setOnClickListener {
            mViewModel.deviceInfo?.let {
                LogMgr.d("deviceInfo:$it")
                showDeviceInfoDialog()
            } ?: ToastTool.showWarningToast(context!!, "暂未获取到设备信息", 0, 30)
        }
        ll_set_speed.setOnClickListener {
            showSetSpeedDialog()
        }
        cl_back.setOnClickListener {
            backView()
        }
    }

    private fun showGoBackHomeDialog() {
        if (mBackToHomedDialog == null) {
            mBackToHomedDialog = DialogUtils.createDoubleBtnDialog(
                    context,
                    "确认让小岚回桩充电？",
                    View.OnClickListener {
                        if (it.id == R.id.confirm) {
                            if (isBatteryLow()) {
                                LogMgr.e("电量不足，请充电")
                                ToastTool.showWarningToast(context!!, "电量不足，请充电！", 0, 30)
                            } else {
                                updateViewType(ViewType.GO_HOME)
                            }
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

    private fun showGotoGetFoodDialog() {
        if (mGotoGetFoodDialog == null) {
            mGotoGetFoodDialog = DialogUtils.createDoubleBtnDialog(
                    context,
                    "确认去取餐点？",
                    View.OnClickListener {
                        if (it.id == R.id.confirm) {
                            mViewModel.refillPoi?.let {
                                if (isBatteryLow()) {
                                    LogMgr.e("电量不足，请充电")
                                    ToastTool.showWarningToast(context!!, "电量不足，请充电！", 0, 30)
                                } else {
                                    updateViewType(ViewType.GOTO_GET_FOOD)
                                }
                            } ?: let {
                                ToastTool.showWarningToast(context!!, "当前地图未设置取餐点，请先设置取餐点", 0, 30)
                            }
                        } else {
                            dismissGotoGetFoodDialog()
                        }
                    })
        }
        if (mGotoGetFoodDialog != null && !mGotoGetFoodDialog!!.isShowing) {
            mGotoGetFoodDialog!!.show()
        }
    }

    private fun dismissGotoGetFoodDialog() {
        mGotoGetFoodDialog?.dismiss()
    }

    private fun showShutDownDialog() {
        if (mShutDownDialog == null) {
            mShutDownDialog = DialogUtils.createDoubleBtnDialog(
                    context,
                    "确认关闭机器人？",
                    View.OnClickListener {
                        if (it.id == R.id.confirm) {
                            reboot()
                        } else {
                            dismissShutDownDialog()
                        }
                    })
        }
        if (mShutDownDialog != null && !mShutDownDialog!!.isShowing) {
            mShutDownDialog!!.show()
        }
    }

    private fun dismissShutDownDialog() {
        mShutDownDialog?.dismiss()
    }

    private fun showDeviceInfoDialog() {
        var deviceInfo = ""
        mViewModel.deviceInfo?.let {
            deviceInfo = "制造商ID : ${it.manufacturerId}\n制造商名称 : ${it.manufacturerName} \n" +
                    "设备型号Id : ${it.modelId} \n设备型号名称 : ${it.modelName} \n" +
                    "设备序列号 : ${it.deviceID} \n硬件版本号 : ${it.hardwareVersion}\n" +
                    "软件版本号 : ${it.softwareVersion}\nApp版本号 : ${context!!.packageManager.getPackageInfo(context!!.packageName, 0).versionName}"
        }
        if (mDeviceInfoDialog == null) {
            mDeviceInfoDialog = DialogUtils.createDeviceInfoDialog(
                    context,
                    "设备信息",
                    deviceInfo,
                    View.OnClickListener {
                        dismissDeviceInfoDialog()
                    })
        }
        if (mDeviceInfoDialog != null && !mDeviceInfoDialog!!.isShowing) {
            mDeviceInfoDialog!!.show()
        }
    }

    private fun dismissDeviceInfoDialog() {
        mDeviceInfoDialog?.dismiss()
    }

    private fun showSetSpeedDialog() {
        if (mSetSpeedDialog == null) {
            mSetSpeedDialog = SetSpeedDialog(context, mViewModel.currentSpeed, R.style.alert_dialog)
            mSetSpeedDialog?.setDialogListener(object : SetSpeedDialog.DialogListener {
                override fun onClickCancel() {
                    dismissSetSpeedDialog()
                }

                override fun onClickConfirm(speed: String) {
                    if (speed.isNotEmpty()) {
                        if (speed.toFloat() in 0.2..0.7) {
                            //设置速度
                            Repository.setSystemParameter(SystemParameter(Constants
                                    .MAX_SPEED_KEY, speed)).observe(this@SettingFragment, Observer {
                                val rp = it.getOrNull()
                                LogMgr.d("set speed result:$rp")
                                dismissSetSpeedDialog()
                                if (rp != null && rp) {
                                    mViewModel.currentSpeed = speed
                                    ToastTool.showWarningToast(context!!, "速度修改成功！", 0, 30)
                                } else {
                                    ToastTool.showWarningToast(context!!, "速度修改失败！", 0, 30)
                                }
                            })
                        } else {
                            ToastTool.showWarningToast(context!!, "请输入0.2-0.7之间的数字", 0, 30)
                        }
                    } else {
                        ToastTool.showWarningToast(context!!, "请输入0.2-0.7之间的数字", 0, 30)
                    }
                }
            })
        }
        if (mSetSpeedDialog != null && !mSetSpeedDialog!!.isShowing) {
            mSetSpeedDialog!!.show()
        }
    }

    private fun dismissSetSpeedDialog() {
        mSetSpeedDialog?.dismiss()
        mSetSpeedDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBackToHomedDialog?.dismiss()
        mBackToHomedDialog?.dismiss()
        mGotoGetFoodDialog?.dismiss()
        mDeviceInfoDialog?.dismiss()
        mSetSpeedDialog?.dismiss()
    }

    private fun reboot() {
        Repository.shutdown(ShutdownRequest(0, 0))
                .observe(this, Observer {
                    val shutdownResponse = it.getOrNull()
                    LogMgr.i("shutdown response $shutdownResponse")
                    shutdownResponse?.let {
                        val isShutdown = shutdownResponse.body() ?: false
                        if (isShutdown) {
                            dismissShutDownDialog()
                            cl_shut_down.visibility = View.VISIBLE
                            Glide.with(context!!).load(R.drawable.tasks).into(iv_shut_down)
                        } else {
                            ToastTool.showWarningToast(context!!, "关机异常!", 0, 30)
                            LogMgr.e("request shut down failed")
                        }
                    } ?: let {
                        ToastTool.showWarningToast(context!!, "关机异常!", 0, 30)
                        LogMgr.e("request shut down failed")
                    }
                })
    }

}