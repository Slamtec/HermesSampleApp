package com.slamtec.robot.deliver.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.slamtec.robot.deliver.R
import com.slamtec.robot.deliver.model.BoxState
import kotlinx.android.synthetic.main.view_box.view.*

class BoxView : RelativeLayout {

    private var mRoomTextView: TextView
    private var mActionTextView: TextView
    private var mBoxBackground: FrameLayout
    var mBoxState: BoxState = BoxState.OPEN

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val v = View.inflate(context, R.layout.view_box, this)
        mBoxBackground = v.findViewById(R.id.fl_box_bg) as FrameLayout
        mRoomTextView = v.findViewById(R.id.tv_box_location) as TextView
        mActionTextView = v.findViewById(R.id.tv_box_open) as TextView
    }

    fun setLocation(location: String) {
        tv_box_location.text = location
    }

    fun setBoxState(boxState: BoxState) {
        mBoxState = boxState
        when (boxState) {
            BoxState.DONE -> {
                v_box_shade.visibility = View.GONE
                lv_box_loading.visibility = View.GONE
                tv_box_loading_tip.visibility = View.GONE
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_grey_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
                mActionTextView.setBackgroundResource(R.drawable.button_grey)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
                mActionTextView.setText(R.string.box_done)
            }
            BoxState.CLOSING -> {
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_blue_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setBackgroundResource(R.drawable.selector_box_bg)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setText(R.string.box_close)
                v_box_shade.visibility = View.VISIBLE
                lv_box_loading.visibility = View.VISIBLE
                tv_box_loading_tip.visibility = View.VISIBLE
            }
            BoxState.CLOSED -> {
                v_box_shade.visibility = View.GONE
                lv_box_loading.visibility = View.GONE
                tv_box_loading_tip.visibility = View.GONE
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_yellow_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setBackgroundResource(R.drawable.selector_box_bg)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setText(R.string.box_close)
            }
            BoxState.OPEN -> {
                v_box_shade.visibility = View.GONE
                lv_box_loading.visibility = View.GONE
                tv_box_loading_tip.visibility = View.GONE
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_blue_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setBackgroundResource(R.drawable.selector_box_bg)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setText(R.string.box_open)
            }
            BoxState.OPENING -> {
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_yellow_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setBackgroundResource(R.drawable.selector_box_bg)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                mActionTextView.setText(R.string.box_open)
                v_box_shade.visibility = View.VISIBLE
                lv_box_loading.visibility = View.VISIBLE
                tv_box_loading_tip.visibility = View.VISIBLE
            }
            BoxState.ERROR -> {
                v_box_shade.visibility = View.GONE
                lv_box_loading.visibility = View.GONE
                tv_box_loading_tip.visibility = View.GONE
                mBoxBackground.setBackgroundResource(R.drawable.shape_box_grey_bg)
                mRoomTextView.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
                mActionTextView.setBackgroundResource(R.drawable.button_grey)
                mActionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
                mActionTextView.setText(R.string.box_error)
            }
        }
    }

}