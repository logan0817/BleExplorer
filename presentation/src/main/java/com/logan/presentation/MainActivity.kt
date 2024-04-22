package com.logan.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alibaba.android.arouter.facade.annotation.Route
import com.logan.common.constant.KEY_INDEX
import com.logan.common.constant.MAIN_ACTIVITY_HOME
import com.logan.framework.base.BaseDataBindActivity
import com.logan.framework.log.LogUtil
import com.logan.framework.toast.TipsToast
import com.logan.framework.utils.AppExit
import com.logan.framework.utils.StatusBarSettingHelper
import com.logan.presentation.databinding.ActivityMainBinding

/**
 * @author logan.gan
 * @time   2023/3/3 8:41
 * @desc   主页
 */
@Route(path = MAIN_ACTIVITY_HOME)
class MainActivity : BaseDataBindActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController

    companion object {
        fun start(context: Context, index: Int = 0) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(KEY_INDEX, index)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.setGraph(R.navigation.mobile_navigation)
        StatusBarSettingHelper.setStatusBarTranslucent(this)
        StatusBarSettingHelper.statusBarLightMode(this@MainActivity, false)
    }

}