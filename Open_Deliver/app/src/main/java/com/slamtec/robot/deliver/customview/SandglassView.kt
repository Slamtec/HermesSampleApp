package com.slamtec.robot.deliver.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat.getColor
import com.slamtec.robot.deliver.R

class SandglassView : View {
    var mLineColor: Int
    var mTime: Int
    var mSize: Float
    var mPaint: Paint
    var mDrawable: Drawable
    var mCenterPoint: Point
    var mProgress = 0
    var mStrokWidth = 6F

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.SandglassView)
        mLineColor = ta.getColor(R.styleable.SandglassView_lineColor, getColor(context, R.color.sandglass_color))
        mTime = ta.getInt(R.styleable.SandglassView_time, 60)
        mSize=ta.getDimension(R.styleable.SandglassView_size, R.dimen.dp_56.toFloat())
        mDrawable = ta.getDrawable(R.styleable.SandglassView_center_drawable)!!
        ta.recycle()
        mPaint = Paint()
        mPaint.color = mLineColor
        mPaint.strokeWidth = mStrokWidth
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.typeface = Typeface.DEFAULT_BOLD
        mCenterPoint = Point((mSize / 2).toInt(), (mSize / 2).toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawSandglass(canvas)
        drawCircle(canvas)
    }

    private fun drawCircle(canvas: Canvas?) {
        val rect = RectF(mStrokWidth / 2, mStrokWidth / 2, mSize.toFloat() - mStrokWidth / 2, mSize.toFloat() - mStrokWidth / 2)
        canvas!!.drawArc(rect, 0F, (360 * mProgress / 100).toFloat(), false, mPaint)
    }

    fun setProgress(progress: Int, durationTime: Long) {
        if (durationTime <= 0) {
            mProgress = progress
            invalidate()
        } else {
            val valueAnimator = ValueAnimator.ofInt(mProgress, progress)
            valueAnimator.addUpdateListener {
                mProgress = it.animatedValue as Int
                invalidate()
            }
            valueAnimator.interpolator = OvershootInterpolator()
            valueAnimator.duration = durationTime
            valueAnimator.start()
        }
    }

    private fun drawSandglass(canvas: Canvas?) {
        val drawable = mDrawable
        drawable.setBounds(
            mCenterPoint.x - drawable.intrinsicWidth / 2, mCenterPoint.y - drawable.intrinsicHeight / 2,
            mCenterPoint.x + drawable.intrinsicWidth / 2, mCenterPoint.y + drawable.intrinsicHeight / 2
        )
        drawable.draw(canvas!!)
    }


}


