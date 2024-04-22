package com.logan.presentation.helper

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattClientScope
import androidx.bluetooth.GattService
import androidx.bluetooth.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object BleManager {

    val TAG = "BleManager"

    val scope = GlobalScope

    //Record all scan results
    val scanDevides: MutableList<ScanResult> = mutableListOf()

    lateinit var context: Application

    private var connectJobMap: MutableMap<String, Job> = mutableMapOf()
    private var gattClientScopeMap: MutableMap<String, GattClientScope> = mutableMapOf()

    val subscribeFlowMap = mutableMapOf<String, Flow<ByteArray>?>()
    val subscribeListenerMap = mutableMapOf<String, (ByteArray) -> Unit>()

    private val bluetoothManager by lazy {
        if (isSupportBle()) {
            context.getSystemService(BluetoothManager::class.java)
        } else {
            null
        }

    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    fun init(application: Application) {
        context = application
    }

    /**
     * is support ble?
     *
     * @return
     */
    fun isSupportBle(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
    }

    /**
     * Open bluetooth
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        bluetoothAdapter?.enable()
    }

    /**
     * Disable bluetooth
     */
    @SuppressLint("MissingPermission")
    fun disableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothAdapter?.disable()
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    fun isBlueEnable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun getConnectJob(mac: String): Job? = connectJobMap[mac]


    fun getGattClient(mac: String): GattClientScope? = gattClientScopeMap[mac]

    fun disconnect(mac: String) {
        gattClientScopeMap.remove(mac)
        connectJobMap.remove(mac)?.cancel()
    }

    fun disconnectAllDevice() {
        gattClientScopeMap.clear()
        connectJobMap.forEach { s, job ->
            job.cancel()
        }
        connectJobMap.clear()
    }

    @SuppressLint("MissingPermission")
    suspend fun <R> connectGatt(
        mac: String,
        block: suspend GattClientScope.() -> R
    ) {
        connectJobMap[mac] = scope.launch {
            val bluetoothLe = BluetoothLe(context)
            getBluetoothDevice(mac)?.let { bluetoothDevice ->
                withContext(Dispatchers.Main) {
                    bluetoothLe.connectGatt(bluetoothDevice) {
                        gattClientScopeMap[mac] = this
                        block.invoke(this)
                    }
                }
                awaitCancellation()
            }
        }
    }

    fun getBluetoothDevice(mac: String): BluetoothDevice? {
        return scanDevides.find { it.deviceAddress.address == mac }?.device
    }

    fun getGattService(
        mac: String,
        characteristicsUUID: UUID
    ): GattService? {
        return getGattClient(mac)?.services?.find {
            it.characteristics.find { it.uuid.toString() == characteristicsUUID.toString() } != null
        }
    }

    fun getCharacteristic(
        mac: String,
        characteristicsUUID: UUID
    ): GattCharacteristic? {
        val batteryService = getGattService(mac, characteristicsUUID)
        return batteryService?.getCharacteristic(characteristicsUUID)
    }

    fun checkCharacteristicpropertyRead(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        return properties and BluetoothGattCharacteristic.PROPERTY_READ != 0
    }


    fun checkCharacteristicpropertyWrite(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        return properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
    }


    fun checkCharacteristicpropertyNotify(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        return properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    fun checkCharacteristicProperties(characteristic: BluetoothGattCharacteristic): Triple<Boolean, Boolean, Boolean> {
        val properties = characteristic.properties
        val a = properties and BluetoothGattCharacteristic.PROPERTY_READ != 0
        val b = properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
        val c = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
        return Triple(a, b, c)
    }

    suspend fun readCharacteristic(mac: String, uuid: UUID): ByteArray? {
        val service = getGattService(mac, uuid)
        service?.getCharacteristic(uuid)?.let { characteristic ->
            val result = getGattClient(mac)?.readCharacteristic(characteristic)
            if (result?.isSuccess == true) {
                val value = result.getOrNull() ?: return null
                return value
            } else {
                return null
            }
        } ?: return null
    }


    suspend fun writeCharacteristic(mac: String, uuid: UUID, bytes: ByteArray): Boolean {
        val service = getGattService(mac, uuid)
        service?.getCharacteristic(uuid)?.let { characteristic ->
            val result = getGattClient(mac)?.writeCharacteristic(characteristic, bytes)
            if (result?.isSuccess == true) {
                return true
            } else {
                return false
            }
        } ?: return false
    }


    fun subscribeToCharacteristic(mac: String, uuid: UUID): Flow<ByteArray>? {
        val findSubscribeFlow = subscribeFlowMap[mac]
        if (subscribeFlowMap.contains(mac)) {
            //Prevent duplicate subscriptions
            return findSubscribeFlow
        } else {
            val service = getGattService(mac, uuid)
            service?.getCharacteristic(uuid)?.let { characteristic ->
                val subscribeFlow = getGattClient(mac)?.subscribeToCharacteristic(characteristic)
                subscribeFlowMap[mac] = subscribeFlow
                scope.launch { executeSubscription(mac, subscribeFlow) }
                return subscribeFlow
            } ?: return null
        }
    }

    suspend fun executeSubscription(mac: String, subscribeFlow: Flow<ByteArray>?) {
        subscribeFlow?.collect {
            withContext(Dispatchers.Main) {
                subscribeListenerMap[mac]?.invoke(it)
            }
        }
    }

    fun addSubscribeFlowCollect(mac: String, block: (ByteArray) -> Unit) {
        subscribeListenerMap[mac] = block
    }

}