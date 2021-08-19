package com.slamtec.robot.deliver

import android.app.Application
import com.tamsiree.rxkit.RxTool

class RobotApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RxTool.init(this).crashProfile().enabled(true).minTimeBetweenCrashesMs(2000)
            .errorActivity(MainActivity::class.java).apply()
    }

}