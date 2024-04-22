package com.logan.presentation.ui.bluetoothdevice.adapter

import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattService
import com.logan.framework.interfaces.MultiItemEntity

class GattServiceEntity(
    override val itemType: Int,
    val title:String,
    val gattService: GattService? = null,
    val characteristic: GattCharacteristic? = null
) : MultiItemEntity