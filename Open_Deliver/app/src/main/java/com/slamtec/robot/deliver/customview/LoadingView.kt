package com.slamtec.robot.deliver.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @Author: hero
 * @CreateDate: 2021/1/21 19:31
 * @Description:
 */
class LoadingView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /**
     * view宽度
     */
    private var mWidth = 0

    /**
     * view高度
     */
    private var mHeight = 0

    /**
     * 菊花的矩形的宽
     */
    private var widthRect = 0

    /**
     * 菊花的矩形的宽
     */
    private var heightRect = 0

    /**
     * 菊花绘制画笔
     */
    private var rectPaint: Paint? = null

    /**
     * 循环绘制位置
     */
    private var pos = 0

    /**
     * 菊花矩形
     */
    private var rect: RectF? = null

    /**
     * 循环颜色
     */
    private val color =
        arrayOf("#bbbbbb", "#aaaaaa", "#999999", "#888888", "#777777", "#666666")

    private fun init() {
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        //根据个人习惯设置  这里设置  如果是wrap_content  则设置为宽高200
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            mWidth = 200
        } else {
            mWidth = MeasureSpec.getSize(widthMeasureSpec)
            mHeight = MeasureSpec.getSize(heightMeasureSpec)
            mWidth = Math.min(mWidth, mHeight)
        }
        widthRect = mWidth / 12 //菊花矩形的宽
        heightRect = 4 * widthRect //菊花矩形的高
        setMeasuredDimension(mWidth, mWidth)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rect == null) {
            rect = RectF(
                ((mWidth - widthRect) / 2).toFloat(),
                0F,
                ((mWidth + widthRect) / 2).toFloat(),
                heightRect.toFloat()
            )
        }
        for (i in 0..11) {
            if (i - pos >= 5) {
                rectPaint!!.color = Color.parseColor(color[5])
            } else if (i - pos >= 0 && i - pos < 5) {
                rectPaint!!.color = Color.parseColor(color[i - pos])
            } else if (i - pos >= -7 && i - pos < 0) {
                rectPaint!!.color = Color.parseColor(color[5])
            } else if (i - pos >= -11 && i - pos < -7) {
                rectPaint!!.color = Color.parseColor(color[12 + i - pos])
            }
            canvas.drawRect(rect!!, rectPaint!!) //绘制
            canvas.rotate(30f, mWidth / 2.toFloat(), mWidth / 2.toFloat()) //旋转
        }
        pos++
        if (pos > 11) {
            pos = 0
        }
        postInvalidateDelayed(100) //一个周期用时1200
    }

    init {
        init()
    }
}