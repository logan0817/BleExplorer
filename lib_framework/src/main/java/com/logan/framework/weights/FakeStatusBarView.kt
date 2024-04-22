package com.logan.framework.weights

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.logan.framework.utils.screenWidth
import com.logan.framework.utils.statusBarHeight

class FakeStatusBarView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(context.screenWidth, context.statusBarHeight)
    }
}