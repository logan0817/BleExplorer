package com.logan.presentation.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.logan.common.provider.MainServiceProvider
import com.logan.framework.base.BaseDataBindActivity
import com.logan.framework.ext.countDownCoroutines
import com.logan.framework.ext.onClick
import com.logan.framework.utils.StatusBarSettingHelper
import com.logan.presentation.R
import com.logan.presentation.databinding.ActivitySplashBinding

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   启动页
 */
class SplashActivity : BaseDataBindActivity<ActivitySplashBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        StatusBarSettingHelper.setStatusBarTranslucent(this)
        mBinding.tvSkip.onClick {
            MainServiceProvider.toMain(this)
        }
        //倒计时
        countDownCoroutines(2, lifecycleScope, onTick = {
            mBinding.tvSkip.text = getString(R.string.splash_time, it.plus(1).toString())
        }) {
            MainServiceProvider.toMain(this)
            finish()
        }
    }
}