package com.logan.presentation.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.logan.common.constant.MAIN_SERVICE_HOME
import com.logan.common.service.IMainService
import com.logan.presentation.MainActivity

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   主页服务
 * 提供对IMainService接口的具体实现
 */
@Route(path = MAIN_SERVICE_HOME)
class MainService : IMainService {
    /**
     * 跳转主页
     * @param context
     * @param index tab位置
     */
    override fun toMain(context: Context, index: Int) {
        MainActivity.start(context, index)
    }

    /**
     * 跳转主页
     * @param context
     * @param url
     * @param title 标题
     */
    override fun toArticleDetail(context: Context, url: String, title: String) {
    }

    override fun init(context: Context?) {
    }
}