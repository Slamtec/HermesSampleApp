package com.slamtec.robot.deliver.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.slamtec.robot.deliver.R

class NumberKeyboardView : LinearLayout {
    private  var m1TV: TextView
    private  var m2TV: TextView
    private  var m3TV: TextView
    private  var m4TV: TextView
    private  var m5TV: TextView
    private  var m6TV: TextView
    private  var m7TV: TextView
    private  var m8TV: TextView
    private  var m9TV: TextView
    private  var m0TV: TextView
    private  var mDelTV: TextView
    private  var mConfirmTV: TextView
    private lateinit var mListener: OnClickListenerBuilder

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val view = View.inflate(context, R.layout.view_keyboard, this)
        m1TV = view.findViewById(R.id.tv_number_1) as TextView
        m1TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('1') }
        m2TV = view.findViewById(R.id.tv_number_2) as TextView
        m2TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('2') }
        m3TV = view.findViewById(R.id.tv_number_3) as TextView
        m3TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('3') }
        m4TV = view.findViewById(R.id.tv_number_4) as TextView
        m4TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('4') }
        m5TV = view.findViewById(R.id.tv_number_5) as TextView
        m5TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('5') }
        m6TV = view.findViewById(R.id.tv_number_6) as TextView
        m6TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('6') }
        m7TV = view.findViewById(R.id.tv_number_7) as TextView
        m7TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('7') }
        m8TV = view.findViewById(R.id.tv_number_8) as TextView
        m8TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('8') }
        m9TV = view.findViewById(R.id.tv_number_9) as TextView
        m9TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('9') }
        mDelTV = view.findViewById(R.id.tv_number_del) as TextView
        mDelTV.setOnClickListener { mListener.mClickDelAction!!.invoke() }
        m0TV = view.findViewById(R.id.tv_number_0) as TextView
        m0TV.setOnClickListener { mListener.mClickNumberAction!!.invoke('0') }
        mConfirmTV = findViewById(R.id.tv_number_confirm) as TextView
        mConfirmTV.setOnClickListener { mListener.mClickConfirmAction!!.invoke() }
    }

    fun setOnClickListener(listenerBuilder: OnClickListenerBuilder.() -> Unit) {
        mListener = OnClickListenerBuilder().also(listenerBuilder)
    }


    inner class OnClickListenerBuilder {
        var mClickNumberAction: ((Char) -> Unit)? = null
        var mClickDelAction: (() -> Unit)? = null
        var mClickConfirmAction: (() -> Unit)? = null

        fun onClickNumber(action: (Char) -> Unit) {
            mClickNumberAction = action
        }

        fun onClickDel(action: () -> Unit) {
            mClickDelAction = action
        }

        fun onClickConfirm(action: () -> Unit) {
            mClickConfirmAction = action
        }
    }

}




