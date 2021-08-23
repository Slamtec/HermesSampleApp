package com.slamtec.fooddelivery.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.slamtec.fooddelivery.R

object ToastTool {
    private var passToast: Toast? = null

    fun showWarningToast(context: Context, errMsg: String, xOffset: Int, yOffset: Int) {
        if (passToast == null) {
            passToast = Toast(context)
        }
        passToast?.view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null)
        val tvError = passToast?.view!!.findViewById(R.id.tv_pwd_error) as TextView
        tvError.text = errMsg
        passToast?.setGravity(Gravity.TOP, xOffset, yOffset)
        passToast?.show()
    }

    fun clearWarningToast() {
        passToast?.cancel()
        passToast = null
    }
}