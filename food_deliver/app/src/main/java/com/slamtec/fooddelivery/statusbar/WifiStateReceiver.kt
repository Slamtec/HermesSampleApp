package com.slamtec.fooddelivery.statusbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.lifecycle.MutableLiveData
import com.slamtec.fooddelivery.model.SignalLevel
import com.slamtec.fooddelivery.utils.LogMgr

class WifiStateReceiver(private val wifiManager: WifiManager) : BroadcastReceiver() {
    private var mSignalLevel = MutableLiveData<SignalLevel>()
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!wifiManager.isWifiEnabled) {
            LogMgr.d("wifi is not enabled!")
            mSignalLevel.value = SignalLevel.NONE
            return
        }
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null && wifiInfo.ssid.isNotEmpty() && wifiInfo.networkId != -1) {
            val level = when (WifiManager.calculateSignalLevel(wifiInfo.rssi, 4)) {
                3 -> SignalLevel.GREAT
                2 -> SignalLevel.GOOD
                1 -> SignalLevel.MODERATE
                else -> SignalLevel.POOR
            }
            LogMgr.d("wifi level : $level")
            mSignalLevel.value = level
        } else {
            LogMgr.d("wifi level is none")
            mSignalLevel.value = SignalLevel.NONE
        }
    }

    fun listenSignalLevel(): MutableLiveData<SignalLevel> {
        return mSignalLevel
    }
}