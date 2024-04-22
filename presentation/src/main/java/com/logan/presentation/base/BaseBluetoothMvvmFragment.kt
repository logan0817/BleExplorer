package com.logan.presentation.base

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.logan.framework.base.BaseMvvmFragment
import com.logan.presentation.R
import com.logan.presentation.helper.checkBleAndGpsStatus
import com.logan.presentation.utils.SnackbarUtils

/**
 * @date   2024/04/15
 * @desc   DataBinding和ViewModel基类
 */
abstract class BaseBluetoothMvvmFragment<DB : ViewDataBinding, VM : ViewModel> :
    BaseMvvmFragment<DB, VM>() {
    var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                suspendFunc?.invoke()
            } else {
            }
            suspendFunc = null
        }

    var bluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[getBluetoothPermissions().first()] == true) {
                suspendFunc?.invoke()
            } else {
            }
            suspendFunc = null
        }

    fun getBluetoothPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    val locationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            suspendFunc?.invoke()
        } else {
        }
        suspendFunc = null
    }

    private val bluetoothStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                updateBluetoothState(state == BluetoothAdapter.STATE_ON)
            }
        }
    }

    fun updateBluetoothState(isEnabled: Boolean) {

    }


    override fun onStart() {
        super.onStart()
        context?.registerReceiver(
            bluetoothStateBroadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun onStop() {
        super.onStop()
        context?.unregisterReceiver(bluetoothStateBroadcastReceiver)
    }


    fun doPermissionsCheck(allowBlock: () -> Unit) {
        doPermissionsCheck(allowBlock, null)
    }

    private var suspendFunc: (() -> Unit?)? = null

    fun doPermissionsCheck(allowBlock: () -> Unit, disableBlock: (() -> Unit)?) {
        context?.checkBleAndGpsStatus(
            gpsNotOpenCall = {
                SnackbarUtils.showActionSnackbar(
                    requireView(),
                    resources.getString(R.string.service_location_title),
                    resources.getString(R.string.sure),
                    resources.getColor(R.color.teal_200),
                ) {
                    suspendFunc = { doPermissionsCheck(allowBlock, disableBlock) }
                    activityResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            },
            bluetoothPermissionErrorCall = {
                SnackbarUtils.showActionSnackbar(
                    requireView(),
                    resources.getString(R.string.permission_ble_title),
                    resources.getString(R.string.sure),
                    resources.getColor(R.color.teal_200),
                ) {
                    suspendFunc = { doPermissionsCheck(allowBlock, disableBlock) }
                    bluetoothPermissionsLauncher.launch(getBluetoothPermissions())
                }
            },
            bluetoothNotOpenCall = {
                SnackbarUtils.showActionSnackbar(
                    requireView(),
                    resources.getString(R.string.service_ble_title),
                    resources.getString(R.string.sure),
                    resources.getColor(R.color.teal_200),
                ) {
                    suspendFunc = { doPermissionsCheck(allowBlock, disableBlock) }
                    activityResultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            },
            gpsPermissionErrorCall = {
                SnackbarUtils.showActionSnackbar(
                    requireView(),
                    resources.getString(R.string.permission_location_title),
                    resources.getString(R.string.sure),
                    resources.getColor(R.color.teal_200),
                ) {
                    suspendFunc = { doPermissionsCheck(allowBlock, disableBlock) }
                    locationPermissionsLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                }
            },
            normalCall = {
                suspendFunc = null
                allowBlock.invoke()
            },
        )
    }

}