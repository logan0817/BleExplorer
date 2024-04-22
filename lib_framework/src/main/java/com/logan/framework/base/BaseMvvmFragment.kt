package com.logan.framework.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

/**
 * @date   2024/04/15
 * @desc   DataBinding和ViewModel基类
 */
abstract class BaseMvvmFragment<DB : ViewDataBinding, VM : ViewModel> : BaseDataBindFragment<DB>() {
    /**
     * If you need to intercept the return event, please handleBackPressedEnable = true ,
     * Then when the user clicks the back button, the onBackPressed method will be executed
     * */
    open var handleBackPressedEnable: Boolean = false

    lateinit var mViewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
        super.onViewCreated(view, savedInstanceState)
    }


    open fun initViewModel() {
        val argument = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        mViewModel = ViewModelProvider(this).get(argument[1] as Class<VM>)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (handleBackPressedEnable) {
            addBackPressedCallback()
        }
    }

    open fun addBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    open fun onBackPressed() {
        Log.w(this::class.qualifiedName, "onBackPressed")
    }

}