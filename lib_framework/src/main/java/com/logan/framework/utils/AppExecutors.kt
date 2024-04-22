package com.logan.framework.utils

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import com.logan.framework.log.LogUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * 线程池组件
 */
object AppExecutors {

    private val threadFactory = ThreadFactory {
        Thread(it).apply {
            priority = android.os.Process.THREAD_PRIORITY_BACKGROUND
            setUncaughtExceptionHandler { t, e ->
                LogUtil.e(
                    "Thread<${t.name}> has uncaughtException",
                    e
                )
            }
        }
    }

    val cpuIO: Executor =
        CpuIOThreadExecutor(threadFactory)
    val diskIO: Executor =
        DiskIOThreadExecutor(threadFactory)
    val mainThread = MainThreadExecutor()

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }

        fun executeDelay(command: Runnable, delayMillis: Long) {
            mainThreadHandler.postDelayed(command, delayMillis)
        }
    }

    private class DiskIOThreadExecutor(threadFactory: ThreadFactory) : Executor {

        private val diskIO = Executors.newSingleThreadExecutor(threadFactory)

        override fun execute(command: Runnable) {
            val className = Throwable().stackTrace[1]?.className ?: "Undefined"
            val methodName = Throwable().stackTrace[1]?.methodName ?: "Undefined"
            diskIO.execute(RunnableWrapper("$className#$methodName", command))
        }
    }

    private class CpuIOThreadExecutor(threadFactory: ThreadFactory) : Executor {

        private val cpuIO = ThreadPoolExecutor(
            2,
            max(2, Runtime.getRuntime().availableProcessors()),
            30,
            TimeUnit.SECONDS,
            ArrayBlockingQueue<Runnable>(128),
            threadFactory,
            object : ThreadPoolExecutor.DiscardOldestPolicy() {
                override fun rejectedExecution(r: Runnable?, e: ThreadPoolExecutor?) {
                    super.rejectedExecution(r, e)
                    LogUtil.e("CpuIOThreadExecutor#rejectedExecution => Runnable <$r>")
                }
            }
        )

        override fun execute(command: Runnable) {
            val name = Throwable().stackTrace[1].className
            cpuIO.execute(RunnableWrapper(name, command))
        }
    }

}

private class RunnableWrapper(private val name: String, private val runnable: Runnable) : Runnable {
    override fun run() {
        Thread.currentThread().name = name
        runnable.run()
    }
}



/**
 * screen width in pixels
 */
val Context.screenWidth
    get() = resources.displayMetrics.widthPixels

/**
 * screen height in pixels
 */
val Context.screenHeight
    get() = resources.displayMetrics.heightPixels

/**
 * 状态栏高度
 */
val Context?.statusBarHeight: Int
    get() {
        this ?: return 0
        var result = 24
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        result = if (resId > 0) {
            resources.getDimensionPixelSize(resId)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                result.toFloat(), Resources.getSystem().displayMetrics
            ).toInt()
        }
        return result
    }
