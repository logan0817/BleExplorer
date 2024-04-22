package com.logan.stater.utils

import com.logan.framework.helper.SumAppHelper
import com.logan.framework.log.LogUtil

object DispatcherLog {
    var isDebug = SumAppHelper.isDebug()

    @JvmStatic
    fun i(msg: String?) {
        if (msg == null) {
            return
        }
        LogUtil.i(msg, tag = "StartTask")
    }
}