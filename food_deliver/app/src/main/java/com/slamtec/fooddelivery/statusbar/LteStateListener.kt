package com.slamtec.fooddelivery.statusbar

import android.net.wifi.WifiManager
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import androidx.lifecycle.MutableLiveData
import com.slamtec.fooddelivery.model.SignalLevel
import com.slamtec.fooddelivery.utils.LogMgr

class LteStateListener : PhoneStateListener() {
    private var mSignalLevel = MutableLiveData<SignalLevel>()
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        super.onSignalStrengthsChanged(signalStrength)
        LogMgr.d("onSignalStrengthsChanged:${signalStrength.toString()}")
        val method = SignalStrength::class.java.getMethod("getLteSignalStrength")
        val dbm = method.invoke(signalStrength) as Int
        val level = WifiManager.calculateSignalLevel(dbm, 4)
        LogMgr.d("dbm:$dbm,level:$level")
        mSignalLevel.value = when (level) {
            3 -> SignalLevel.GREAT
            2 -> SignalLevel.GOOD
            1 -> SignalLevel.MODERATE
            else -> SignalLevel.POOR
        }
    }

    fun listenSignalLevel(): MutableLiveData<SignalLevel> {
        return mSignalLevel
    }

}