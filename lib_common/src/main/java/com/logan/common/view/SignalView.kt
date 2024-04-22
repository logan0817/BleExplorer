package com.logan.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.logan.common.R
import kotlin.math.min

/**
 * 自定义信号强弱显示控件
 *
 *
 * Created by jianddongguo on 2017/8/10.
 */
class SignalView : View {
    private var rectOffset = 0
    private var mRadius = 0
    private var signalValue = 0
    private var signalType: String? = null
    private var mPaint: Paint? = null
    private var mRectHeight = 0
    private var mRectWidth = 0
    private var mRectCount = 0
    private var mSignalLowColor = 0
    private var mSignalMiddleColor = 0
    private var mSignalHighColor = 0
    private var mRectBorderColor = 0
    private var mRectBorderWidth = 0
    private var mSignalTypeTextColor = 0
    private var mSignalTypeTextSize = 0f

    // 在Java代码中实例化使用控件
    constructor(context: Context?) : super(context)

    /**
     * 在XML代码中使用控件自动回调
     * 获取XML中自定义属性
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SignalView)
        // 信号柱数目,默认5条
        mRectCount = ta.getInt(R.styleable.SignalView_signalCount, 7)
        mRadius = ta.getInt(R.styleable.SignalView_signalRadius, 3)
        // 信号柱间隔，默认4dp
        rectOffset = ta.getInt(R.styleable.SignalView_signalRectInterval, 4)
        // 信号柱宽度，默认1dp
        mRectBorderWidth = ta.getInt(R.styleable.SignalView_rectBorderWidth, 0)
        // 信号柱背景颜色，默认黑色
        mRectBorderColor = ta.getColor(
            R.styleable.SignalView_rectBackgroundColor,
            resources.getColor(R.color.colorBlack)
        )
        // 弱信号颜色，默认红色
        mSignalLowColor =
            ta.getColor(R.styleable.SignalView_signalLowColor, resources.getColor(R.color.colorRed))
        // 中等信号颜色，默认黄色
        mSignalMiddleColor = ta.getColor(
            R.styleable.SignalView_signalMiddleColor,
            resources.getColor(R.color.colorYellow)
        )
        // 强信号颜色，默认绿色
        mSignalHighColor = ta.getColor(
            R.styleable.SignalView_signalHighColor,
            resources.getColor(R.color.colorGreen)
        )

        // 信号类型文本字体颜色，默认黑色
        mSignalTypeTextColor = ta.getColor(
            R.styleable.SignalView_signalTypeTextColor,
            resources.getColor(R.color.colorBlack)
        )
        // 信号类型文本字体大小，默认12sp
        mSignalTypeTextSize = ta.getDimension(R.styleable.SignalView_signalTypeTextSize, 14f)
        ta.recycle()
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
    }

    fun setSignalValue(signalValue: Int) {
        // 如果值大于信号柱，抛出异常
        if (signalValue > mRectCount) {
            Throwable("setSignalValue method value error,can not exceed settings value!")
        }
        this.signalValue = signalValue
        // 更新值后，重新绘制SignalView
        this.invalidate()
    }

    fun setSignalTypeText(signalType: String?) {
        this.signalType = signalType
    }

    // 当View大小改变时，获取View相关属性，初始化
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRectHeight = height
        mRectWidth = width / mRectCount
    }

    /**
     * 测量
     * 如果不覆写onMeasure方法，SignalView值支持EXACTY模式(具体值或match_parent)
     * 不支持AT_MOST模式，即wrap_content
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        var height = 0
        // 测量模式，从xml可知
        val specMode = MeasureSpec.getMode(heightMeasureSpec)
        // 测量大小,从xml中获取
        val specSize = MeasureSpec.getSize(heightMeasureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            height = specSize
        } else {
            height = 70
            // wrap_content模式，选择最小值
            if (specMode == MeasureSpec.AT_MOST) {
                height = min(height.toDouble(), specSize.toDouble()).toInt()
            }
        }
        mRectHeight = height
        return height
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        var width = 0
        // 测量模式，从xml可知
        val specMode = MeasureSpec.getMode(widthMeasureSpec)
        // 测量大小,从xml中获取
        val specSize = MeasureSpec.getSize(widthMeasureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize
        } else {
            width = 70
            // wrap_content模式，选择最小值
            if (specMode == MeasureSpec.AT_MOST) {
                width = min(width.toDouble(), specSize.toDouble()).toInt()
            }
        }
        return width
    }

    /**
     * 绘制
     * 绘制信号条，根据强弱显示不同颜色
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制信号类型文字
        if (!TextUtils.isEmpty(signalType)) {
            mPaint!!.color = mSignalTypeTextColor
            mPaint!!.textSize = mSignalTypeTextSize
            mPaint!!.strokeWidth = 1f
            mPaint!!.style = Paint.Style.FILL
            canvas.drawText(signalType!!, 0f, mSignalTypeTextSize, mPaint!!)
        }
        mPaint!!.strokeWidth = mRectBorderWidth.toFloat()
        // 绘制信号条，根据强弱显示不同颜色
        for (i in 0 until mRectCount) {
            // 前signalValue信号柱绘制实心颜色
            if (i < signalValue) {
                if (signalValue <= mRectCount / 3) {
                    mPaint!!.color = mSignalLowColor
                } else if (signalValue <= mRectCount * 2 / 3) {
                    mPaint!!.color = mSignalMiddleColor
                } else {
                    mPaint!!.color = mSignalHighColor
                }
                mPaint!!.style = Paint.Style.FILL
            } else {
                mPaint!!.color = mRectBorderColor
//                mPaint!!.style = Paint.Style.STROKE
                mPaint!!.style = Paint.Style.FILL
            }
            // 绘制信号矩形柱
            canvas.drawRoundRect(
                (mRectWidth * i + rectOffset).toFloat(),
                (mRectHeight * (mRectCount - i) * 0.13).toFloat(),
                (mRectWidth * (i + 1)).toFloat(),
                mRectHeight.toFloat(),
                mRadius.toFloat(),
                mRadius.toFloat(),
                mPaint!!
            )
        }
    }
}