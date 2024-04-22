package com.logan.presentation.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat


sealed class BleStatusExtension() {
    object NotSupportBLE : BleStatusExtension()
    object BluetoothPermissionError : BleStatusExtension()
    object BluetoothNotOpen : BleStatusExtension()
    object GpsNotOpen : BleStatusExtension()
    object GpsPermissionError : BleStatusExtension()

    object Normal : BleStatusExtension()
}


fun Context.checkBleAndGpsStatus(
    gpsNotOpenCall: () -> Unit,
    gpsPermissionErrorCall: () -> Unit,
    notSupportBLECall: () -> Unit = {},
    bluetoothPermissionErrorCall: () -> Unit,
    bluetoothNotOpenCall: () -> Unit,
    normalCall: () -> Unit
) {
    when (this.getBleAndGpsStatus()) {
        BleStatusExtension.NotSupportBLE -> {
            notSupportBLECall.invoke()
        }
        BleStatusExtension.BluetoothPermissionError -> {
            bluetoothPermissionErrorCall.invoke()
        }
        BleStatusExtension.GpsNotOpen -> {
            gpsNotOpenCall.invoke()
        }
        BleStatusExtension.BluetoothNotOpen -> {
            bluetoothNotOpenCall.invoke()
        }
        BleStatusExtension.GpsPermissionError -> {
            gpsPermissionErrorCall.invoke()
        }
        BleStatusExtension.Normal -> {
            normalCall.invoke()
        }
    }

}


fun Context.getBleAndGpsStatus(): BleStatusExtension {
    val supportBle = BleManager.isSupportBle()
    val blueEnable = BleManager.isBlueEnable()
    val gpsEnable = isGpsEnable(this)
    val hasGpsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        true
    } else {
        isPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        isPermission(this, Manifest.permission.BLUETOOTH_SCAN) && isPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        true
    }
    return if (!supportBle) {
        BleStatusExtension.NotSupportBLE
    } else if (!hasBluetoothPermission) {
        BleStatusExtension.BluetoothPermissionError
    } else if (!blueEnable) {
        BleStatusExtension.BluetoothNotOpen
    } else if (!gpsEnable) {
        BleStatusExtension.GpsNotOpen
    } else if (!hasGpsPermission) {
        BleStatusExtension.GpsPermissionError
    } else {
        BleStatusExtension.Normal
    }

}

fun isGpsEnable(context: Context): Boolean {
    return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isGpsOpen(context))
}

fun isGpsOpen(context: Context): Boolean {
    var locationMode = 0
    val locationProviders: String
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        locationMode = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        locationMode != Settings.Secure.LOCATION_MODE_OFF
    } else {
        locationProviders = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        !TextUtils.isEmpty(locationProviders)
    }
}

fun isPermission(context: Context, permission: String): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) === PackageManager.PERMISSION_GRANTED
}
