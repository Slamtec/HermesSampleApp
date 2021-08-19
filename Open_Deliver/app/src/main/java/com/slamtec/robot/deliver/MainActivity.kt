package com.slamtec.robot.deliver

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.slamtec.robot.deliver.fragment.*
import com.slamtec.robot.deliver.fragment.setting.SettingFragment
import com.slamtec.robot.deliver.model.DeliveringInfo
import com.slamtec.robot.deliver.model.DeviceError
import com.slamtec.robot.deliver.model.RobotViewType
import com.slamtec.robot.deliver.model.RobotViewType.*
import com.slamtec.robot.deliver.model.TaskInfo
import com.slamtec.robot.deliver.utils.LogMgr
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import kotlinx.coroutines.*


class MainActivity : FragmentActivity() {
    private val mRobotViewModel by lazy { ViewModelProvider(this).get(RobotViewModel::class.java) }
    private var mCurrentViewType: RobotViewType? = null
    private var mSettingTimeoutJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requirePermission()
        hideBar()
        setContentView(R.layout.activity_main)
    }


    private fun requirePermission() {
        AndPermission.with(this).runtime().permission(
            Permission.ACCESS_FINE_LOCATION, Permission.WRITE_EXTERNAL_STORAGE
            , Permission.READ_EXTERNAL_STORAGE
        ).onDenied {
            LogMgr.e("permission denied")
            finish()
        }.onGranted {
            LogMgr.d("permission granted")
            toggleRobotView()
            refreshDeliveryStage()
            refreshPowerStatus()
        }.start()
    }

    private fun toggleRobotView() {
        mRobotViewModel.viewType.value = HOME
        mRobotViewModel.viewType.observe(this, Observer {
            LogMgr.i("current view type $mCurrentViewType, update to view type $it")
            if (mCurrentViewType != it) {
                mCurrentViewType = it
                when (it) {
                    HOME -> replaceFragment(HomeFragment.newInstance(), R.id.fl_main_fragment)
                    INPUT -> replaceFragment(InputFragment.newInstance(), R.id.fl_main_fragment)
                    LOW_BATTERY -> replaceFragment(LowBatteryFragment.newInstance(), R.id.fl_main_fragment)
                    OPEN -> replaceFragment(BoxOpenFragment.newInstance(), R.id.fl_main_fragment)
                    CLOSE -> replaceFragment(BoxCloseFragment.newInstance(), R.id.fl_main_fragment)
                    PINCH_ERROR -> replaceFragment(BoxPinchFragment.newInstance(), R.id.fl_main_fragment)
                    CREATE -> replaceFragment(CreateFragment.newInstance(), R.id.fl_main_fragment)
                    SEND -> replaceFragment(SendFragment.newInstance(), R.id.fl_main_fragment)
                    DELIVER -> replaceFragment(DeliverFragment.newInstance(), R.id.fl_main_fragment)
                    GOING_HOME -> replaceFragment(DeliverFragment.newInstance(), R.id.fl_main_fragment)
                    ARRIVED -> replaceFragment(ArrivedFragment.newInstance(), R.id.fl_main_fragment)
                    TAKEOUT_PICKUP -> replaceFragment(TakeoutPickupFragment.newInstance(), R.id.fl_main_fragment)
                    ERROR -> replaceFragment(ErrorFragment.newInstance(), R.id.fl_main_fragment)
                    RETRIEVE -> replaceFragment(
                        RetrieveFragment.newInstance(),
                        R.id.fl_main_fragment
                    )
                    ERROR_RETRIEVE -> replaceFragment(ErrorRetrieveFragment.newInstance(), R.id.fl_main_fragment)
                    SETTING -> replaceFragment(SettingFragment.newInstance(), R.id.fl_main_fragment)
                }
            }
        })


    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
        beginTransaction().func().commit()

    private fun replaceFragment(fragment: Fragment, fragmentId: Int) =
        supportFragmentManager.inTransaction { replace(fragmentId, fragment) }

    fun getRobotViewModel(): RobotViewModel {
        return mRobotViewModel
    }



    private fun refreshDeliveryStage() {
        mRobotViewModel.queryDeliveryStage().observe(this, Observer {
            val deliveryStageResponse = it.getOrNull()
            if (deliveryStageResponse != null) {
                mRobotViewModel.deliveryStageResponse.value = deliveryStageResponse
                mRobotViewModel.deliveryStage.value = deliveryStageResponse.stage
                when (deliveryStageResponse.stage) {
                    DeliveryStage.IDLE -> {
                        if (mCurrentViewType == GOING_HOME
                            || mCurrentViewType == ARRIVED || mCurrentViewType == DELIVER || mCurrentViewType == RETRIEVE || mCurrentViewType == ERROR
                        ) {
                            mRobotViewModel.updateViewType(HOME)
                        }
                    }

                    DeliveryStage.ON_DELIVERING -> {
                        if (mCurrentViewType != DELIVER) {
                            val jsonString = Gson().toJson(deliveryStageResponse.info)
                            jsonString?.let {
                                mRobotViewModel.deliveringInfo = Gson().fromJson(jsonString, DeliveringInfo::class.java)
                            }
                            mRobotViewModel.updateViewType(DELIVER)
                        }
                    }
                    DeliveryStage.GOING_HOME -> {
                        if (mCurrentViewType != GOING_HOME) {
                            mRobotViewModel.updateViewType(GOING_HOME)
                        }
                    }

                    DeliveryStage.ARRIVED_AT_TARGET -> {
                        if (mCurrentViewType == DELIVER || mCurrentViewType == HOME) {
                            val jsonString = Gson().toJson(deliveryStageResponse.info)
                            jsonString?.let {
                                val taskInfo = Gson().fromJson(jsonString, TaskInfo::class.java)
                                LogMgr.d("taskInfo $taskInfo  viewType ${mCurrentViewType.toString()}")
                                if (handleCheck(taskInfo)) {
                                    mRobotViewModel.taskInfo = taskInfo
                                    mRobotViewModel.updateViewType(ARRIVED)
                                }
                            }
                        }
                    }
                    DeliveryStage.DELIVERY_CANCELLED -> mRobotViewModel.updateViewType(RETRIEVE)
                    DeliveryStage.BACK_TO_STATION -> mRobotViewModel.updateViewType(HOME)
                    DeliveryStage.DEVICE_ERROR -> {
                        if (mCurrentViewType != ERROR && mCurrentViewType != SETTING ) {
                            val jsonString = Gson().toJson(deliveryStageResponse.info)
                            jsonString?.let {
                                LogMgr.d("device json string $jsonString")
                                val deviceError = Gson().fromJson(jsonString, DeviceError::class.java)
                                LogMgr.d("deviceError $deviceError  viewType ${mCurrentViewType.toString()}")
                                mRobotViewModel.deviceError = deviceError
                                mRobotViewModel.deviceError?.let {
                                    mRobotViewModel.updateViewType(ERROR)
                                }
                            }
                        }
                    }
                }
                LogMgr.i("deliveryStage:${deliveryStageResponse}")
            }
        })
    }

    private fun handleCheck(taskInfo: TaskInfo): Boolean {
        if (taskInfo == null) {
            return false
        }
        val preTaskInfo = mRobotViewModel.taskInfo ?: return true
        if (preTaskInfo.id != taskInfo.id) {
            return true
        }
        return false
    }

    private fun refreshPowerStatus() {
        mRobotViewModel.queryPowerStatus().observe(this, Observer {
            val powerStatus = it.getOrNull()
            mRobotViewModel.powerStatus = powerStatus
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mSettingTimeoutJob?.cancel()
    }


    private fun hideBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != currentFocus) {
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            checkSettingTimeout()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun checkSettingTimeout() {
        mSettingTimeoutJob?.cancel()
        mSettingTimeoutJob = GlobalScope.launch(Dispatchers.Main) {
            delay(5 * 60 * 1000)
            if (mCurrentViewType == SETTING) {
                if (mRobotViewModel.deliveryStage.value == DeliveryStage.DEVICE_ERROR) {
                    mRobotViewModel.updateViewType(ERROR)
                } else {
                    mRobotViewModel.updateViewType(HOME)
                }
            }
        }
    }

}
