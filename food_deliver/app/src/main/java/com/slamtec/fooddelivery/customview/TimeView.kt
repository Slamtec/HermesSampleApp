package com.slamtec.fooddelivery.customview

import android.content.Context
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.slamtec.fooddelivery.R
import com.slamtec.fooddelivery.model.DateViewBean
import kotlinx.android.synthetic.main.view_date.view.*

/**
 * File   : TimeView
 * Author : Qikun.Xiong
 * Date   : 2021/8/13 6:35 PM
 */
class TimeView : LinearLayout {

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.view_date, this)
    }

    fun refreshDateView(dateViewBean: DateViewBean) {
        tv_date_day.text = dateViewBean.day
        tv_date_week.text = dateViewBean.week
        tv_date_time.text = dateViewBean.time
    }
}