package com.slamtec.fooddelivery.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.MutableLiveData
import com.slamtec.fooddelivery.model.SignalLevel

class StatusBarListener(private val context: Context) {

    private lateinit var mWifiIntentFilter: IntentFilter
    private lateinit var mWifiReceiver: WifiStateReceiver
    private lateinit var m4GIntentFilter: IntentFilter
    private lateinit var m4GListener: LteStateListener
    private lateinit var mAudioIntentFilter: IntentFilter
    private lateinit var mWifiManager: WifiManager
    private lateinit var mAudioManager: AudioManager
    lateinit var mTelephonyManager: TelephonyManager

    private var mLetSignalLevel = MutableLiveData<SignalLevel>()

    fun register() {
        //监听wifi变化状态
        mWifiIntentFilter = IntentFilter()
        mWifiIntentFilter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK)
        mWifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        mWifiIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        mWifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        mWifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        mWifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiReceiver = WifiStateReceiver(mWifiManager)
        context.registerReceiver(mWifiReceiver, mWifiIntentFilter)
        //监听4G信号强度变化
        m4GIntentFilter = IntentFilter()
        m4GIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        m4GListener = LteStateListener()
        mTelephonyManager.listen(m4GListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        //监听声音强弱变化
        mAudioIntentFilter = IntentFilter()
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun unRegister() {
        mTelephonyManager.listen(m4GListener, PhoneStateListener.LISTEN_NONE)
        context.unregisterReceiver(mWifiReceiver)
    }

    inner class LteListener : PhoneStateListener() {
        @SuppressLint("MissingPermission")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            if (mTelephonyManager.networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                //lte signal
            }
        }
    }

    fun listenWifiState(): MutableLiveData<SignalLevel> {
        return mWifiReceiver.listenSignalLevel()
    }

    fun listen4GState(): MutableLiveData<SignalLevel> {
        return m4GListener.listenSignalLevel()
    }
}