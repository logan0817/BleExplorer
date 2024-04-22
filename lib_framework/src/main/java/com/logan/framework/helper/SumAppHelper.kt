package com.logan.framework.helper

import android.app.Application

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   提供应用环境
 */
object SumAppHelper {
    private lateinit var app: Application
    private var isDebug = false

    fun init(application: Application, isDebug: Boolean) {
        this.app = application
        this.isDebug = isDebug
    }

    /**
     * 获取全局应用
     */
    fun getApplication() = app

    /**
     * 是否为debug环境
     */
    fun isDebug() = isDebug
}