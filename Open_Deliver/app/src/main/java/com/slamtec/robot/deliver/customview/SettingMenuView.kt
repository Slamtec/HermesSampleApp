package com.slamtec.robot.deliver.customview

import android.content.Context
import android.media.Image
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.slamtec.robot.deliver.R

class SettingMenuView : RelativeLayout {
    private var mCofigRL: RelativeLayout
    private var mConfigIV: ImageView
    private var mConfigTV: TextView
    private var mCargoRL: RelativeLayout
    private var mCargoIV: ImageView
    private var mCargoTV: TextView
    private lateinit var mListenerBuilder: OnSelectListenerBuilder

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val v = View.inflate(context, R.layout.view_setting_menu, this)
        mCofigRL = v.findViewById(R.id.rl_setting_config) as RelativeLayout
        mCofigRL.setOnClickListener {
            mListenerBuilder.mSelectConfigAction?.invoke()
        }
        mConfigIV = v.findViewById(R.id.iv_setting_config) as ImageView
        mConfigTV = v.findViewById(R.id.tv_setting_config) as TextView
        mCargoRL = v.findViewById(R.id.rl_setting_cargo) as RelativeLayout
        mCargoRL.setOnClickListener {
            mListenerBuilder.mSelectCargoAction?.invoke()
        }
        mCargoIV = v.findViewById(R.id.iv_setting_cargo) as ImageView
        mCargoTV = v.findViewById(R.id.tv_setting_cargo) as TextView
        selectConfig()
    }

     fun selectConfig() {
        mCofigRL.setBackgroundResource(R.drawable.setting_nav_menu_bg)
        mConfigIV.setImageResource(R.drawable.setting_nav_administration_on)
        mConfigTV.setTextColor(ContextCompat.getColor(context, R.color.text_blue_setting))
        mCargoRL.setBackgroundResource(R.color.transparent)
        mCargoIV.setImageResource(R.drawable.setting_nav_open)
        mCargoTV.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

     fun selectCargo() {
        mCofigRL.setBackgroundResource(R.color.transparent)
        mConfigIV.setImageResource(R.drawable.setting_nav_administration)
        mConfigTV.setTextColor(ContextCompat.getColor(context, R.color.white))
        mCargoRL.setBackgroundResource(R.drawable.setting_nav_menu_bg)
        mCargoIV.setImageResource(R.drawable.setting_nav_open_on)
        mCargoTV.setTextColor(ContextCompat.getColor(context, R.color.text_blue_setting))
    }


    fun setOnSelectListener(listenerBuilder: OnSelectListenerBuilder.() -> Unit) {
        mListenerBuilder = OnSelectListenerBuilder().also(listenerBuilder)
    }

    inner class OnSelectListenerBuilder {
        var mSelectConfigAction: (() -> Unit)? = null
        var mSelectCargoAction: (() -> Unit)? = null

        fun onSelectConfig(action: () -> Unit) {
            mSelectConfigAction = action
        }

        fun onSelectCargo(action: () -> Unit) {
            mSelectCargoAction = action
        }
    }
}