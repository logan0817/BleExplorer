package com.logan.bleexplorer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDex
import com.logan.bleexplorer.task.InitAppManagerTask
import com.logan.bleexplorer.task.InitArouterTask
import com.logan.bleexplorer.task.InitMmkvTask
import com.logan.bleexplorer.task.InitRefreshLayoutTask
import com.logan.bleexplorer.task.InitSumHelperTask
import com.logan.framework.log.LogUtil
import com.logan.framework.manager.ActivityManager
import com.logan.framework.manager.AppFrontBack
import com.logan.framework.manager.AppFrontBackListener
import com.logan.framework.toast.TipsToast
import com.logan.presentation.helper.BleManager
import com.logan.stater.dispatcher.TaskDispatcher

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   Application
 */
class MyApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        BleManager.init(this)
        //注册APP前后台切换监听
        appFrontBackRegister()
        // App启动立即注册监听
        registerActivityLifecycle()
        TipsToast.init(this)

        //1.启动器：TaskDispatcher初始化
        TaskDispatcher.init(this)
        //2.创建dispatcher实例
        val dispatcher: TaskDispatcher = TaskDispatcher.createInstance()

        //3.添加任务并且启动任务
        dispatcher.addTask(InitSumHelperTask(this))
                .addTask(InitMmkvTask())
                .addTask(InitAppManagerTask())
                .addTask(InitRefreshLayoutTask())
                .addTask(InitArouterTask())
                .start()

        //4.等待，需要等待的方法执行完才可以往下执行
        dispatcher.await()
    }

    /**
     * 注册APP前后台切换监听
     */
    private fun appFrontBackRegister() {
        AppFrontBack.register(this, object : AppFrontBackListener {
            override fun onBack(activity: Activity?) {
                LogUtil.d("onBack")
            }

            override fun onFront(activity: Activity?) {
                LogUtil.d("onFront")
            }
        })
    }

    /**
     * 注册Activity生命周期监听
     */
    private fun registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                ActivityManager.pop(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
                ActivityManager.push(activity)
            }

            override fun onActivityResumed(activity: Activity) {
            }
        })
    }
}