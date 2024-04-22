package com.logan.framework.utils

import android.graphics.drawable.ColorDrawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.logan.framework.helper.SumAppHelper

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   资源获取工具类
 */
fun getStringArrayFromResource(@ArrayRes arrayId: Int): Array<String> {
    return SumAppHelper.getApplication().resources.getStringArray(arrayId)
}

fun getStringFromResource(@StringRes stringId: Int): String {
    return SumAppHelper.getApplication().getString(stringId)
}

fun getStringFromResource(@StringRes stringId: Int, vararg formatArgs: Any?): String? {
    return SumAppHelper.getApplication().getString(stringId, *formatArgs)
}

fun getColorFromResource(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(SumAppHelper.getApplication(), colorRes)
}

fun getDimensionFromResource(@DimenRes dimenRes: Int): Int {
    return SumAppHelper.getApplication().resources
            .getDimensionPixelSize(dimenRes)
}

fun getColorDrawable(@ColorRes colorRes: Int): ColorDrawable? {
    return ColorDrawable(
        ContextCompat.getColor(
            SumAppHelper.getApplication(),
            colorRes
        )
    )
}