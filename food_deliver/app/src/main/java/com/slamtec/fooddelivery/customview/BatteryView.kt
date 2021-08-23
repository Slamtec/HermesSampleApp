package com.slamtec.fooddelivery.customview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.slamtec.fooddelivery.R

class BatteryView : View {
    /**
     * 自定义View的宽
     */
    private var mWidth = 0

    /**
     * 自定义View的高
     */
    private var mHeight = 0

    /**
     * 抗锯齿标志
     */
    private var drawFilter: DrawFilter? = null


    /**
     * 电池高度
     */
    private var batteryLevel = DEFAULT_LEVEL

    /**
     * 电池外壳和电池等级直接的间距
     */
    private var distance = 0F
    private var batteryWidth = 0F
    private var batteryHeight = 0F
    private var batteryRadius = 0F


    /**
     * 电池电量 画笔
     */
    private var levelPaint: Paint = Paint()

    /**
     * 电池电量
     */
    private var levelRect: RectF = RectF()


    private var batteryColor = 0

    private var mBackgroundDrawable: Drawable? = null


    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        initTypeArray(context, attrs)
        //设置 电池画笔的相关属性
        levelPaint = Paint()
        levelPaint.color = batteryColor
        levelPaint.style = Paint.Style.FILL
        levelRect = RectF()
    }

    /**
     * 初始化自定义属性
     */
    private fun initTypeArray(context: Context, @Nullable attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryView)
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.BatteryView_backgroundDrawable)
        batteryColor = typedArray.getColor(R.styleable.BatteryView_batteryColor, ContextCompat.getColor(context, android.R.color.black))
        distance = typedArray.getDimension(R.styleable.BatteryView_distance, R.dimen.dp_0_5.toFloat())
        batteryHeight = typedArray.getDimension(R.styleable.BatteryView_batteryHeight, R.dimen.dp_10.toFloat())
        batteryWidth = typedArray.getDimension(R.styleable.BatteryView_batteryWidth, R.dimen.dp_26.toFloat())
        batteryRadius = typedArray.getDimension(R.styleable.BatteryView_batteryRadius, R.dimen.dp_0_5.toFloat())
        //回收typedArray
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //对View上的內容进行测量后得到的View內容占据的宽度
        mWidth = measuredWidth
        //对View上的內容进行测量后得到的View內容占据的高度
        mHeight = measuredHeight
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawFilter = drawFilter
        levelPaint.color = batteryColor
        // 电池电量 矩形的坐标
        levelRect.left = distance
        levelRect.top = distance
        levelRect.right = distance + (batteryLevel / MAX_LEVEL) * batteryWidth
        levelRect.bottom = distance + batteryHeight


        //绘制电池等级

        drawBackground(canvas)
        canvas.drawRoundRect(levelRect, batteryRadius, batteryRadius, levelPaint)
        //drawIcon(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable!!.setBounds(0, 0, mBackgroundDrawable!!.intrinsicWidth, mBackgroundDrawable!!.intrinsicHeight)
            mBackgroundDrawable!!.draw(canvas)
        }
    }


    /**
     * 设置电池电量
     *
     * @param level
     */
    fun setBatteryLevel(level: Float) {
        batteryLevel = level
        if (batteryLevel < 0) {
            batteryLevel = MAX_LEVEL
        } else if (batteryLevel > MAX_LEVEL) {
            batteryLevel = MAX_LEVEL
        }
        postInvalidate()
    }

    fun setBatteryColor(color: Int) {
        batteryColor = ContextCompat.getColor(context, color)
        invalidate()
    }


    companion object {
        /**
         * 电池最大电量
         */
        const val MAX_LEVEL = 100F

        /**
         * 电池默认电量
         */
        const val DEFAULT_LEVEL = 100F
    }
}