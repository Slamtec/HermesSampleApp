package com.slamtec.robot.deliver.utils

import android.content.Context

object AppUtils {

    fun getVersionName(context: Context): String {
        try {
            val packageManager = context.packageManager
            val info = packageManager.getPackageInfo(context.packageName, 0)
            return info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "1.1.1"
    }

}