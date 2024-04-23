package com.logan.presentation.ui.bluetoothdevice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.bluetooth.GattService
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.logan.framework.adapter.BaseBindViewHolder
import com.logan.framework.adapter.BaseMultiItemAdapter
import com.logan.framework.ext.onClick
import com.logan.presentation.R
import com.logan.presentation.base.BaseBluetoothMvvmFragment
import com.logan.presentation.bluetooth.GattAttributes
import com.logan.presentation.bluetooth.UUIDUtils
import com.logan.presentation.bluetooth.convertData
import com.logan.presentation.bluetooth.hexStringToByteArray
import com.logan.presentation.bluetooth.toHexString
import com.logan.presentation.databinding.FragmentBluetoothdeviceBinding
import com.logan.presentation.databinding.ItemGattserviceEntityType0Binding
import com.logan.presentation.databinding.ItemGattserviceEntityType1Binding
import com.logan.presentation.databinding.ItemGattserviceEntityType2Binding
import com.logan.presentation.helper.BleManager
import com.logan.presentation.ui.bluetoothdevice.adapter.GattServiceEntity
import com.logan.presentation.ui.bluetoothdevice.viewmodel.BlueToothViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

class BluetoothDeviceFragment :
    BaseBluetoothMvvmFragment<FragmentBluetoothdeviceBinding, BlueToothViewModel>() {

    private val args: BluetoothDeviceFragmentArgs by navArgs()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        mBinding?.titleBar?.setMiddleText(args.name)
        mBinding?.titleBar?.setBackLayerClick {
            findNavController().popBackStack()
        }
        mBinding?.titleBar?.setRightLayerClick {
            if (mViewModel.readAllCharacteristicLiveData.value != true) {
                mViewModel.readAllCharacteristicLiveData.value = true
            }

        }
        initRecyclerView()
        if (BleManager.getConnectJob(args.mac) == null) {
            showLoading(resources.getString(R.string.device_connecting))
            connect(args.mac)
        }
        initObserve()
    }

    private fun initObserve() {
        mViewModel.readAllCharacteristicLiveData.observe(this) {
            if (it) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(mac: String) = mViewModel.viewModelScope.launch {
        BleManager.connectGatt(mac) {
//            setupPair()
            adapter.setData(getRecycleViewData(services))
            dismissLoading()
            //一旦lambda表达式支持完成，连接就会被断开。要否定这一点，需要在最后通过调用来挂起lambda。这杨还能控制设备何时断开连接
            awaitCancellation()
        }
    }


    fun disconnect() {
        BleManager.disconnect(args.mac)
    }


    @SuppressLint("MissingPermission")
    fun getRecycleViewData(services: List<GattService>): Collection<GattServiceEntity> {
        val list = mutableListOf<GattServiceEntity>()
        list.add(GattServiceEntity(0, "Advertisement Data"))
        list.add(GattServiceEntity(2, "${getString(R.string.device_name)}：${args.name}"))
        list.add(GattServiceEntity(2, "${getString(R.string.device_address)}：${args.mac}"))
        BleManager.scanDevides.find { it.deviceAddress.address == args.mac }?.let {
//            it.getManufacturerSpecificData()
//            厂商 ID 是一个 16 位的整数，范围从 0 到 65535。
            for (i in 0..65535) {
                val datas = it.getManufacturerSpecificData(i)
                if (datas?.isNotEmpty() == true) {
                    list.add(
                        GattServiceEntity(
                            2,
                            "${getString(R.string.manufacturerdata)}：\nCompany:Reserved ID<0x${i.toHexString()}> 0x${datas.toHexString()}"
                        )
                    )
                }
            }
        }

        services.forEach { service ->
            list.add(
                GattServiceEntity(
                    0, GattAttributes.lookup(service.uuid), service, null
                )
            )
            service.characteristics.forEach { characteristic ->
                list.add(
                    GattServiceEntity(
                        1, GattAttributes.lookup(characteristic.uuid), service, characteristic
                    )
                )
            }
        }
        return list
    }


    val adapter = object : BaseMultiItemAdapter<GattServiceEntity>() {
        override fun onBindDefViewHolder(
            holder: BaseBindViewHolder<ViewBinding>, item: GattServiceEntity?, position: Int
        ) {
            holder.setIsRecyclable(false)
            when (getItemViewType(position)) {
                0 -> {
                    val binding = holder.binding as ItemGattserviceEntityType0Binding
                    binding.tvTitle.text = item?.title
                }

                2 -> {
                    val binding = holder.binding as ItemGattserviceEntityType2Binding
                    binding.tvTitle.text = item?.title
                }

                1 -> {
                    holder.itemView.onClick {
                        findNavController().navigate(
                            BluetoothDeviceFragmentDirections.actionBluetoothDeviceFragmentToCharacteristicFragment(
                                args.mac, args.name, item?.characteristic?.uuid.toString()
                            )
                        )
                    }

                    val binding = holder.binding as ItemGattserviceEntityType1Binding
                    binding.tvTitle.text = item?.title
                    if (mViewModel.readAllCharacteristicLiveData.value == true) {
                        mViewModel.viewModelScope.launch {
                            item?.characteristic?.uuid?.let { uuid ->
                                BleManager.getCharacteristic(
                                    args.mac,
                                    uuid
                                )?.fwkCharacteristic?.let {
                                    val readEnable = BleManager.checkCharacteristicpropertyRead(it)
                                    if (readEnable) {
                                        binding.tvSubTitle.text =
                                            BleManager.readCharacteristic(args.mac, uuid)
                                                ?.convertData().toString()
                                    }
                                }
                                if (position == getData().size - 1) {
                                    mViewModel.readAllCharacteristicLiveData.value = false
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun getViewBinding(
            layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int
        ): ViewBinding {
            return when (viewType) {
                0 -> ItemGattserviceEntityType0Binding.inflate(layoutInflater, parent, false)
                1 -> ItemGattserviceEntityType1Binding.inflate(layoutInflater, parent, false)
                2 -> ItemGattserviceEntityType2Binding.inflate(layoutInflater, parent, false)
                else -> ItemGattserviceEntityType0Binding.inflate(
                    layoutInflater, parent, false
                )
            }
        }

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        mBinding?.recyclerView?.layoutManager = layoutManager
        mBinding?.recyclerView?.adapter = adapter
    }

    override fun onDestroyView() {
        mViewModel.readAllCharacteristicLiveData.value = false
        super.onDestroyView()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

}