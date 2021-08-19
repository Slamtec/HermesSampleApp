package com.slamtec.robot.deliver.customview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.slamtec.robot.deliver.R


class EmptyBoxView : RelativeLayout {
    private var iconDrawable: StateListDrawable? = null
    private var boxId: String? = null
    private var boxIdTextColor: ColorStateList? = null
    private var shadeColor: ColorStateList? = null
    private var boxIdTextSize: Float
    private var shadeWidth: Float
    private var shadeHeight: Float
    private var leftMargin: Float
    private var topMargin: Float
    private var cornerRadius: Float
    private var centerPoint: Point
    private var paint: Paint

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.BoxViewEmpty)
        iconDrawable = ta.getDrawable(R.styleable.BoxViewEmpty_iconDrawable) as StateListDrawable?
        boxId = ta.getString(R.styleable.BoxViewEmpty_boxId)
        boxIdTextColor = ta.getColorStateList(
            R.styleable.BoxViewEmpty_boxTextColor
        )
        shadeColor = ta.getColorStateList(
            R.styleable.BoxViewEmpty_shadeColor
        )
        boxIdTextSize =
            ta.getDimension(R.styleable.BoxViewEmpty_boxTextSize, R.dimen.dp_36.toFloat())
        shadeWidth = ta.getDimension(R.styleable.BoxViewEmpty_shadeWidth, R.dimen.dp_194.toFloat())
        shadeHeight =
            ta.getDimension(R.styleable.BoxViewEmpty_shadeHeight, R.dimen.dp_194.toFloat())
        leftMargin = ta.getDimension(R.styleable.BoxViewEmpty_leftMargin, R.dimen.dp_18.toFloat())
        topMargin = ta.getDimension(R.styleable.BoxViewEmpty_topMargin, R.dimen.dp_12.toFloat())
        cornerRadius =
            ta.getDimension(R.styleable.BoxViewEmpty_cornerRadius, R.dimen.dp_12.toFloat())
        ta.recycle()
        centerPoint =
            Point((shadeWidth / 2 + leftMargin).toInt(), (shadeHeight / 2 + topMargin).toInt())
        paint = Paint()
        shadeColor?.let { paint.color = shadeColor!!.defaultColor }
        paint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawShade(canvas)
        drawIcon(canvas)
        drawBoxText(canvas)
    }

    private fun drawIcon(canvas: Canvas?) {
        iconDrawable?.selectDrawable(if (isPressed) 0 else 1)
        val drawable = iconDrawable?.current
        drawable?.let {
            drawable.setBounds(
                centerPoint.x - drawable.intrinsicWidth / 2,
                centerPoint.y + drawable.intrinsicHeight / 2,
                centerPoint.x + drawable.intrinsicWidth / 2,
                centerPoint.y - drawable.intrinsicHeight / 2
            )
            if (canvas != null) {
                drawable.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        postInvalidate()
        return super.onTouchEvent(event)
    }





    private fun drawShade(canvas: Canvas?) {
        shadeColor?.let {
            paint.color = shadeColor!!.getColorForState(drawableState, shadeColor!!.defaultColor)
            val shadeRect = RectF(
                centerPoint.x - shadeWidth / 2,
                centerPoint.y + shadeHeight / 2,
                centerPoint.x + shadeWidth / 2,
                centerPoint.y - shadeHeight / 2
            )
            canvas?.drawRoundRect(shadeRect, cornerRadius, cornerRadius, paint)
        }
    }

    private fun drawBoxText(canvas: Canvas?) {
        boxId?.let {
            boxIdTextColor?.let {
                paint.color =
                    boxIdTextColor!!.getColorForState(drawableState, boxIdTextColor!!.defaultColor)
                paint.textSize = boxIdTextSize
                val bounds = Rect()
                paint.getTextBounds(boxId, 0, boxId!!.length, bounds)
                val fontMetrics: FontMetricsInt = paint.fontMetricsInt
                val x: Int = measuredWidth / 2 - bounds.width() / 2
                val y =
                    (measuredHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top
                canvas?.drawText(boxId!!, x.toFloat(), y.toFloat(), paint)
            }
        }
    }

}