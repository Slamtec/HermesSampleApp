package com.slamtec.fooddelivery.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.slamtec.fooddelivery.R

/**
 * File   : SetSpeedDialog
 * Author : Qikun.Xiong
 * Date   : 2021/8/13 5:29 PM
 */
class SetSpeedDialog(context: Context?, val currentSpeed: String?, style: Int) : Dialog(context!!, style) {
    private var listener: DialogListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_speed)
        val confirm = findViewById<TextView>(R.id.confirm)
        val cancel = findViewById<TextView>(R.id.cancel)
        val et = findViewById<TextView>(R.id.et)
        val tvCurrentSpeed = findViewById<TextView>(R.id.tv_current_speed)
        tvCurrentSpeed.text = currentSpeed
        confirm.setOnClickListener {
            listener?.onClickConfirm(et.text.toString())
        }
        cancel.setOnClickListener {
            listener?.onClickCancel()
        }
    }

    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }

    interface DialogListener {
        fun onClickConfirm(speed: String)
        fun onClickCancel()
    }
}