package com.slamtec.fooddelivery.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.api.Repository
import com.slamtec.fooddelivery.constants.Constants
import com.slamtec.fooddelivery.model.*
import com.slamtec.fooddelivery.statusbar.StatusBarListener
import com.slamtec.fooddelivery.utils.DateUtils
import com.slamtec.fooddelivery.utils.LogMgr
import com.slamtec.fooddelivery.utils.SoundPoolUtil
import com.slamtec.fooddelivery.utils.ToastTool
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.layout_status_bar.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * File   : MainActivity
 * Author : Qikun.Xiong
 * Date   : 2021/8/6 5:45 PM
 */
class MainActivity : FragmentActivity() {

    private val mViewModel by lazy { ViewModelProvider(this).get(FoodDeliverViewModel::class.java) }
    private var mStatusBarListener: StatusBarListener? = null
    private var mCurrentViewType: ViewType? = null
    private var mDateTimer: Timer? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)
                .subscribe {
                    if (it) {
                        SoundPoolUtil.getInstance(this).playSoundWithRedId(R.raw.self_introduction)
                        LogMgr.d("permission is granted!")
                        mViewModel.initPlate()
                        queryPois()
                        initHomeFragment()
                        refreshStatusBar()
                    } else {
                        LogMgr.e("permission denied")
                    }
                }
    }

    private fun queryPois() {
        Repository.queryPois().observe(this, androidx.lifecycle.Observer { result ->
            val pois = result.getOrNull()
            LogMgr.i("queryPois : $pois")
            pois?.let {
                val roomList = pois.filter { PoiType.ROOM == PoiType.valueOf(it.type) }
                val roomNameList = roomList.map { room -> room.poi_name }
                mViewModel.mRoomList.clear()
                mViewModel.mRoomList.addAll(roomNameList)
                val refillStation = pois.find { PoiType.REFILL == PoiType.valueOf(it.type) }
                refillStation?.let {
                    mViewModel.refillPoi = refillStation.poi_name
                } ?: let {
                    LogMgr.e("未设置取餐点！")
                    ToastTool.showWarningToast(this, "当前地图未设置取餐点，请先设置取餐点", 0, 30)
                }
            }
        })
    }

    private fun initHomeFragment() {
        mViewModel.viewType.value = ViewType.HOME
        mViewModel.viewType.observe(this, Observer {
            if (mCurrentViewType != it) {
                mCurrentViewType = it
                when (it) {
                    ViewType.INPUT_TABLE -> switchFragment(InputTableFragment.newInstance())
                    ViewType.SETTING -> switchFragment(SettingFragment.newInstance())
                    ViewType.HOME -> switchFragment(HomeFragment.newInstance())
                    ViewType.DELIVERING -> switchFragment(DeliveringFragment.newInstance())
                    ViewType.ARRIVED_TABLE -> switchFragment(ArrivedFragment.newInstance())
                    ViewType.GOTO_GET_FOOD -> switchFragment(GotoGetFoodFragment.newInstance())
                    ViewType.GO_HOME -> switchFragment(GoHomeFragment.newInstance())
                    ViewType.ERROR -> switchFragment(RobotErrorFragment.newInstance())
                    else -> switchFragment(HomeFragment.newInstance())
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refreshStatusBar() {
        refreshDateTimer()
        mStatusBarListener = StatusBarListener(this)
        mStatusBarListener?.register()
        mStatusBarListener?.listenWifiState()?.observe(this, Observer {
            LogMgr.d("wifi signal: $it")
            if (it == SignalLevel.NONE) {
                iv_status_bar_wifi.visibility = View.GONE
            } else {
                iv_status_bar_wifi.visibility = View.VISIBLE
                val imgId = when (it) {
                    SignalLevel.GREAT -> R.drawable.wifi_great_white
                    SignalLevel.GOOD -> R.drawable.wifi_good_white
                    SignalLevel.MODERATE -> R.drawable.wifi_moderate_white
                    else -> R.drawable.wifi_poor_white
                }
                iv_status_bar_wifi.setImageResource(imgId)
            }
        })
        mStatusBarListener?.listen4GState()?.observe(this, Observer {
            LogMgr.d("4g signal: $it")
            if (it == SignalLevel.NONE) {
                iv_status_bar_4g.visibility = View.GONE
            } else {
                val imgId = when (it) {
                    SignalLevel.POOR -> R.drawable.lte_poor_white
                    SignalLevel.MODERATE -> R.drawable.lte_moderate_white
                    SignalLevel.GOOD -> R.drawable.lte_good_white
                    SignalLevel.GREAT -> R.drawable.lte_great_white
                    else -> R.drawable.lte_great_white
                }
                iv_status_bar_4g.setImageResource(imgId)
            }
        })
        Repository.queryPowerStatus().observe(this, Observer {
            val powerStatus = it.getOrNull()
            mViewModel.powerStatus = powerStatus
            if (powerStatus != null) {
                bv_status_battery.setBatteryLevel(powerStatus.batteryPercentage.toFloat())
                tv_status_bar_battery.text = "${powerStatus.batteryPercentage}%"
                if (powerStatus.isCharging) {
                    bv_status_battery.setBatteryColor(R.color.battery_green)
                    iv_battery_icon.visibility = View.VISIBLE
                    iv_battery_icon.setImageResource(R.drawable.icon_battery_lightning)
                } else {
                    if (powerStatus.dockingStatus == "on_dock") {
                        bv_status_battery.setBatteryColor(R.color.battery_green)
                        iv_battery_icon.visibility = View.VISIBLE
                        iv_battery_icon.setImageResource(R.drawable.icon_battery_plug)
                    } else {
                        iv_battery_icon.visibility = View.GONE
                        bv_status_battery.setBatteryColor(android.R.color.white)
                    }
                }
            }
        })
        Repository.queryRobotHealth().observe(this, Observer {
            val robotHealth = it.getOrNull()
            if (robotHealth != null) {
                mViewModel.robotHealth.value = robotHealth
            }
        })
        Repository.getDeviceInfo().observe(this, Observer {
            val deviceInfo = it.getOrNull()
            LogMgr.i("deviceInfo:$deviceInfo")
            mViewModel.deviceInfo = deviceInfo
        })
        Repository.getSystemParameter(Constants.MAX_SPEED_KEY).observe(this, Observer {
            val currentSpeed = it.getOrNull()
            LogMgr.i("currentSpeed:$currentSpeed")
            mViewModel.currentSpeed = currentSpeed
        })
    }

    private fun refreshDateTimer() {
        mDateTimer = Timer()
        mDateTimer?.schedule(timerTask {
            runOnUiThread {
                val week = DateUtils.formatToWeek(System.currentTimeMillis())
                val day = DateUtils.formatToDay(System.currentTimeMillis())
                val time = DateUtils.formatToTime(System.currentTimeMillis())
                timer_view.refreshDateView(DateViewBean(time, day, week))
            }
        }, 0, 1000)
    }

    fun getViewModel(): FoodDeliverViewModel {
        return mViewModel
    }

    private fun switchFragment(fragment: Fragment) {
        replaceFragment(fragment, R.id.fl_main_fragment)
    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
            beginTransaction().func().commit()

    private fun replaceFragment(fragment: Fragment, fragmentId: Int) =
            supportFragmentManager.inTransaction { replace(fragmentId, fragment) }

    override fun onDestroy() {
        super.onDestroy()
        mStatusBarListener?.unRegister()
        mDateTimer?.cancel()
        mDateTimer = null
    }

}