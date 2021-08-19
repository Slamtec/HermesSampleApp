package com.slamtec.robot.deliver.customview

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.slamtec.robot.deliver.R

object DialogUtils {
    fun createTipDialog(context: Context?, tip: String, listener: View.OnClickListener): Dialog {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.widget_alterable_dialog, null) // 得到加载view
        val confirmTV = v.findViewById(R.id.tv_alterable_confirm) as TextView
        val contentTV = v.findViewById(R.id.tv_alterable_content) as TextView
        contentTV.text = tip
        confirmTV.setOnClickListener(listener)
        val alertDialog = Dialog(context!!, R.style.alert_dialog)
        alertDialog.setCancelable(false) // 不可以用“返回键”取消
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setContentView(v)
        return alertDialog
    }

    fun createTipDialog(context: Context?, tip: String, text: String, listener: View.OnClickListener): Dialog {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.widget_alterable_dialog, null) // 得到加载view
        val confirmTV = v.findViewById(R.id.tv_alterable_confirm) as TextView
        confirmTV.text = text
        val contentTV = v.findViewById(R.id.tv_alterable_content) as TextView
        contentTV.text = tip
        confirmTV.setOnClickListener(listener)
        val alertDialog = Dialog(context!!, R.style.alert_dialog)
        alertDialog.setCancelable(false) // 不可以用“返回键”取消
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setContentView(v)
        return alertDialog
    }

    fun createChooseDialog(context: Context?, tip: String, choose1Text: String, choose2Text: String?, listener: View.OnClickListener): Dialog {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.widget_choose_dialog, null) // 得到加载view
        val content = v.findViewById(R.id.tv_choose_content) as TextView
        content.text = tip
        val choose1 = v.findViewById(R.id.tv_choose_1) as TextView
        choose1.text = choose1Text
        choose1.setOnClickListener(listener)
        if (choose2Text != null) {
            val choose2 = v.findViewById(R.id.tv_choose_2) as TextView
            choose2.text = choose2Text
            choose2.setOnClickListener(listener)
        } else {
            val choose2 = v.findViewById(R.id.tv_choose_2) as TextView
            choose2.visibility = View.GONE
        }
        val alertDialog = Dialog(context!!, R.style.alert_dialog)
        alertDialog.setCancelable(false) // 不可以用“返回键”取消
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setContentView(v)
        return alertDialog
    }
}