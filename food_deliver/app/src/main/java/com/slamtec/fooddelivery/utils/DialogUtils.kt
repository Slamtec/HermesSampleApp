package com.slamtec.fooddelivery.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.slamtec.fooddelivery.R

object DialogUtils {
    fun createDoubleBtnDialog(context: Context?, tips: String?, listener: View.OnClickListener): Dialog {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.dialog_double_btn, null) // 得到加载view
        val tv1 = v.findViewById(R.id.cancel) as TextView
        tv1.setOnClickListener(listener)
        val tv2 = v.findViewById(R.id.confirm) as TextView
        tv2.setOnClickListener(listener)
        val tvTips = v.findViewById(R.id.tv_tips) as TextView
        tvTips.text = tips
        /*val tvTitle = v.findViewById(R.id.tv) as TextView
        tvTitle.text = title*/
        val dialog = Dialog(context!!, R.style.alert_dialog)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(v)
        return dialog
    }

    fun createDeviceInfoDialog(context: Context?, title: String?, tips: String?, listener: View.OnClickListener): Dialog {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.dialog_single_btn, null) // 得到加载view
        val tv2 = v.findViewById(R.id.confirm) as TextView
        tv2.setOnClickListener(listener)
        val tvTips = v.findViewById(R.id.tv_tips) as TextView
        tvTips.text = tips
        val tvTitle = v.findViewById(R.id.tv) as TextView
        tvTitle.text = title
        val dialog = Dialog(context!!, R.style.alert_dialog)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(v)
        return dialog
    }
}