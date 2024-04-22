package com.logan.presentation.ui.bluetoothdevice.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   首页ViewModel
 */
class BlueToothViewModel : ViewModel() {
    val readAllCharacteristicLiveData = MutableLiveData<Boolean>()


}