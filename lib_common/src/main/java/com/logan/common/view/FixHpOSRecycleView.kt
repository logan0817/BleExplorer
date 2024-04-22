package com.logan.common.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/*修复小米澎湃OS在使用时候滑动会异常导致崩溃*/
class FixHpOSRecycleView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        try {
            return super.fling(velocityX, velocityY)
        } catch (ex: Throwable) {
            return false
        }
    }

}