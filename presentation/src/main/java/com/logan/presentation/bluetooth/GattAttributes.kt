package com.logan.presentation.bluetooth

import java.util.*

object GattAttributes {
    private val attributes = HashMap<String, String>()

    init {
        // 标准 GATT 服务
        attributes["00001800-0000-1000-8000-00805f9b34fb"] = "Generic Access"
        attributes["00001801-0000-1000-8000-00805f9b34fb"] = "Generic Attribute"
        attributes["00001802-0000-1000-8000-00805f9b34fb"] = "Immediate Alert"
        attributes["00001803-0000-1000-8000-00805f9b34fb"] = "Link Loss"
        attributes["00001804-0000-1000-8000-00805f9b34fb"] = "Tx Power"
        attributes["00001805-0000-1000-8000-00805f9b34fb"] = "Current Time Service"
        attributes["00001806-0000-1000-8000-00805f9b34fb"] = "Reference Time Update Service"
        attributes["00001807-0000-1000-8000-00805f9b34fb"] = "Next DST Change Service"
        attributes["00001808-0000-1000-8000-00805f9b34fb"] = "Glucose"
        attributes["00001809-0000-1000-8000-00805f9b34fb"] = "Health Thermometer"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information"
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate"
        attributes["0000180e-0000-1000-8000-00805f9b34fb"] = "Phone Alert Status Service"
        attributes["0000180f-0000-1000-8000-00805f9b34fb"] = "Battery Service"
        attributes["00001810-0000-1000-8000-00805f9b34fb"] = "Blood Pressure"
        attributes["00001811-0000-1000-8000-00805f9b34fb"] = "Alert Notification Service"
        attributes["00001812-0000-1000-8000-00805f9b34fb"] = "Human Interface Device"
        attributes["00001813-0000-1000-8000-00805f9b34fb"] = "Scan Parameters"
        attributes["00001814-0000-1000-8000-00805f9b34fb"] = "Running Speed and Cadence"
        attributes["00001815-0000-1000-8000-00805f9b34fb"] = "Cycling Speed and Cadence"
        // 添加更多的标准 GATT 服务...

        // Generic Access 服务的特征
        attributes["00002a00-0000-1000-8000-00805f9b34fb"] = "Device Name"
        attributes["00002a01-0000-1000-8000-00805f9b34fb"] = "Appearance"
        attributes["00002a02-0000-1000-8000-00805f9b34fb"] = "Peripheral Privacy Flag"
        attributes["00002a03-0000-1000-8000-00805f9b34fb"] = "Reconnection Address"
        attributes["00002a04-0000-1000-8000-00805f9b34fb"] =
            "Peripheral Preferred Connection Parameters"
        // 添加更多的 Generic Access 服务的特征...

        // Generic Attribute 服务的特征
        attributes["00002a05-0000-1000-8000-00805f9b34fb"] = "Service Changed"
        // 添加更多的 Generic Attribute 服务的特征...

        // Immediate Alert 服务的特征
        attributes["00002a06-0000-1000-8000-00805f9b34fb"] = "Alert Level"

        // Link Loss 服务的特征
        attributes["00002a06-0000-1000-8000-00805f9b34fb"] = "Alert Level"

        // Tx Power 服务的特征
        attributes["00002a07-0000-1000-8000-00805f9b34fb"] = "Tx Power Level"

        // Current Time Service 服务的特征
        attributes["00002a0f-0000-1000-8000-00805f9b34fb"] = "Exact Time 256"
        attributes["00002a14-0000-1000-8000-00805f9b34fb"] = "Reference Time Information"
        // 添加更多的 Current Time Service 服务的特征...

        // Battery Service 服务的特征
        attributes["00002a19-0000-1000-8000-00805f9b34fb"] = "Battery Level"

        // Blood Pressure 服务的特征
        attributes["00002a35-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Measurement"
        attributes["00002a36-0000-1000-8000-00805f9b34fb"] = "Intermediate Cuff Pressure"
        attributes["00002a49-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Feature"

        // Heart Rate 服务的特征
        attributes["00002a37-0000-1000-8000-00805f9b34fb"] = "Heart Rate Measurement"
        attributes["00002a38-0000-1000-8000-00805f9b34fb"] = "Body Sensor Location"
        attributes["00002a39-0000-1000-8000-00805f9b34fb"] = "Heart Rate Control Point"

        // Device Information 服务的特征
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
        attributes["00002a24-0000-1000-8000-00805f9b34fb"] = "Model Number String"
        attributes["00002a25-0000-1000-8000-00805f9b34fb"] = "Serial Number String"
        attributes["00002a27-0000-1000-8000-00805f9b34fb"] = "Hardware Revision String"
        attributes["00002a26-0000-1000-8000-00805f9b34fb"] = "Firmware Revision String"
        attributes["00002a28-0000-1000-8000-00805f9b34fb"] = "Software Revision String"
        attributes["00002a23-0000-1000-8000-00805f9b34fb"] = "System ID"
        attributes["00002a2a-0000-1000-8000-00805f9b34fb"] =
            "IEEE 11073-20601 Regulatory Certification Data List"
        attributes["00002a50-0000-1000-8000-00805f9b34fb"] = "PnP ID"
        // 添加更多的 Device Information 服务的特征...

        // 添加更多标准服务的特征...
    }

    fun lookup(uuid: UUID): String {
        val name = attributes[uuid.toString()]
        return name ?: uuid.toString()
    }

    fun lookup(uuid: String): String {
        val name = attributes[uuid]
        return name ?: uuid
    }
}
