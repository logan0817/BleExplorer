package com.logan.common.provider

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.launcher.ARouter
import com.logan.common.constant.MAIN_SERVICE_HOME
import com.logan.common.service.IMainService

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   MainService提供类，对外提供相关能力
 * 任意模块就能通过MainServiceProvider使用对外暴露的能力
 */
object MainServiceProvider {

    @Autowired(name = MAIN_SERVICE_HOME)
    lateinit var mainService: IMainService

    init {
        ARouter.getInstance().inject(this)
    }

    /**
     * 跳转主页
     * @param context
     * @param index tab位置
     */
    fun toMain(context: Context, index: Int = 0) {
        mainService.toMain(context, index)
    }

    /**
     * 跳转文章详情
     * @param context
     * @param url 文章链接
     * @param title 标题
     */
    fun toArticleDetail(context: Context, url: String, title: String) {
        mainService.toArticleDetail(context, url, title)
    }
}