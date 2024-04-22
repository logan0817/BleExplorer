package com.logan.presentation.ui.characteristic

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.bluetooth.GattClientScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.logan.framework.adapter.BaseBindViewHolder
import com.logan.framework.adapter.BaseRecyclerViewAdapter
import com.logan.framework.ext.onClick
import com.logan.presentation.R
import com.logan.presentation.base.BaseBluetoothMvvmFragment
import com.logan.presentation.bluetooth.GattAttributes
import com.logan.presentation.bluetooth.asciiToHex
import com.logan.presentation.bluetooth.convertData
import com.logan.presentation.bluetooth.hexStringToByteArray
import com.logan.presentation.bluetooth.isAsciiString
import com.logan.presentation.bluetooth.isHexString
import com.logan.presentation.databinding.FragmentCharacteristicBinding
import com.logan.presentation.databinding.ItemGattserviceEntityType3Binding
import com.logan.presentation.helper.BleManager
import com.logan.presentation.ui.bluetoothdevice.viewmodel.BlueToothViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class CharacteristicFragment :
    BaseBluetoothMvvmFragment<FragmentCharacteristicBinding, BlueToothViewModel>() {

    private var gattClientScope: GattClientScope? = null
    private val args: CharacteristicFragmentArgs by navArgs()

    var subscribeJob: Job? = null

    override fun initView(view: View, savedInstanceState: Bundle?) {
        gattClientScope = BleManager.getGattClient(args.mac)
        mBinding?.titleBar?.setMiddleText(args.name)
        mBinding?.titleBar?.setBackLayerClick {
            findNavController().popBackStack()
        }
        initDeviceInfo()
        initRecyclerView()
    }

    private fun initDeviceInfo() {
        mBinding?.tvMac?.text = args.mac
        mBinding?.tvServiceUUID?.text = BleManager.getGattService(
            args.mac, UUID.fromString(args.characteristic)
        )?.uuid.toString()
        mBinding?.tvCharacteristicName?.text = GattAttributes.lookup(args.characteristic)
        mBinding?.tvCharacteristicUUID?.text = args.characteristic
        BleManager.getCharacteristic(
            args.mac, UUID.fromString(args.characteristic)
        )?.fwkCharacteristic?.let { fwkCharacteristic ->
            val result = BleManager.checkCharacteristicProperties(fwkCharacteristic)
            mBinding?.ivReadable?.setImageResource(
                if (result.first) {
                    R.drawable.ic_right
                } else {
                    R.drawable.ic_wrong
                }
            )
            mBinding?.ivWritable?.setImageResource(
                if (result.second) {
                    R.drawable.ic_right
                } else {
                    R.drawable.ic_wrong
                }
            )
            mBinding?.ivNotifyable?.setImageResource(
                if (result.third) {
                    R.drawable.ic_right
                } else {
                    R.drawable.ic_wrong
                }
            )
            mBinding?.btnRead?.visibility = if (result.first) {
                View.VISIBLE
            } else {
                View.GONE
            }
            mBinding?.btnNotify?.visibility = if (result.third) {
                View.VISIBLE
            } else {
                View.GONE
            }

            mBinding?.llWriteLayout?.visibility = if (result.second) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        mBinding?.btnRead?.onClick {
            mViewModel.viewModelScope.launch {
                val result =
                    BleManager.readCharacteristic(args.mac, UUID.fromString(args.characteristic))
                result?.let { result ->
                    readAdapter.addItem(result)
                    mBinding?.readRecyclerView?.scrollToPosition(readAdapter.getData().size - 1)
                }
            }
        }
        setupNotifyButton(false)


        mBinding?.btnWrite?.onClick {
            mViewModel.viewModelScope.launch {
                val inputString = mBinding?.etWriteInput?.text
                if (!inputString.isNullOrBlank() && inputString.length % 2 == 0) {
                    val bytes = inputString.toString().hexStringToByteArray()
                    val result = BleManager.writeCharacteristic(
                        args.mac, UUID.fromString(args.characteristic), bytes
                    )
                    if (result) {
                        writeAdapter.addItem(bytes)
                        mBinding?.writeRecyclerView?.scrollToPosition(writeAdapter.getData().size - 1)
                        //Automatically read after writing
                        mBinding?.btnRead?.performClick()
                    } else {
                        showToast(getString(R.string.write_failed) + ":$inputString")
                    }
                } else {
                    showToast(getString(R.string.please_check_input) + ":${inputString.toString()}")
                }
            }
        }
    }

    fun setupNotifyButton(isSubscribeing: Boolean) {
        if (isSubscribeing) {
            mBinding?.btnNotify?.text = getString(R.string.unsubscribe)
            mBinding?.btnNotify?.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.color_b2d0e9))
            mBinding?.btnNotify?.onClick {
                setupNotifyButton(false)
            }
        } else {
            mBinding?.btnNotify?.text = getString(R.string.subscribe)
            mBinding?.btnNotify?.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.color_0159a5))
            mBinding?.btnNotify?.onClick {
                setupNotifyButton(true)
            }
        }


        subscribeJob?.cancel()
        subscribeJob = mViewModel.viewModelScope.launch {
            BleManager.subscribeToCharacteristic(args.mac, UUID.fromString(args.characteristic))
            BleManager.addSubscribeFlowCollect(args.mac) { result ->
                lifecycleScope.launchWhenResumed {
                    if (mBinding?.btnNotify?.text == getString(R.string.unsubscribe)) {
                        readAdapter.addItem(result)
                        mBinding?.readRecyclerView?.scrollToPosition(readAdapter.getData().size - 1)
                    }
                }
            }
        }
    }


    val readAdapter =
        object : BaseRecyclerViewAdapter<ByteArray, ItemGattserviceEntityType3Binding>() {
            override fun onBindDefViewHolder(
                holder: BaseBindViewHolder<ItemGattserviceEntityType3Binding>,
                item: ByteArray?,
                position: Int
            ) {
                when (getItemViewType(position)) {
                    0 -> {
                        val binding = holder.binding

                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val currentTime = Calendar.getInstance().time
                        val formattedTime = dateFormat.format(currentTime)
                        binding.tvSubTitle.text = formattedTime

                        binding.tvTitle.text = item?.convertData().toString()
                        holder.itemView.onClick {
                            var input = binding.tvTitle.text.toString()

                            if (input.isHexString()) {
                                mBinding?.etWriteInput?.setText(input)
                                mBinding?.etWriteInput?.setSelection(input.length)
                            } else if (input.isAsciiString()) {
                                input = input.asciiToHex()
                                mBinding?.etWriteInput?.setText(input)
                                mBinding?.etWriteInput?.setSelection(input.length)
                            }
                        }
                    }
                }
            }

            override fun getViewBinding(
                layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int
            ): ItemGattserviceEntityType3Binding {
                return ItemGattserviceEntityType3Binding.inflate(layoutInflater, parent, false)
            }

        }

    val writeAdapter =
        object : BaseRecyclerViewAdapter<ByteArray, ItemGattserviceEntityType3Binding>() {
            override fun onBindDefViewHolder(
                holder: BaseBindViewHolder<ItemGattserviceEntityType3Binding>,
                item: ByteArray?,
                position: Int
            ) {
                when (getItemViewType(position)) {
                    0 -> {
                        val binding = holder.binding
                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val currentTime = Calendar.getInstance().time
                        val formattedTime = dateFormat.format(currentTime)
                        binding.tvSubTitle.text = formattedTime

                        binding.tvTitle.text = item?.convertData().toString()
                        holder.itemView.onClick {
                            val input = binding.tvTitle.text.toString()
                            if (input.isHexString()) {
                                mBinding?.etWriteInput?.setText(input)
                                mBinding?.etWriteInput?.setSelection(input.length)
                            }
                        }
                    }
                }
            }

            override fun getViewBinding(
                layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int
            ): ItemGattserviceEntityType3Binding {
                return ItemGattserviceEntityType3Binding.inflate(layoutInflater, parent, false)
            }

        }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        mBinding?.readRecyclerView?.layoutManager = layoutManager
        mBinding?.readRecyclerView?.adapter = readAdapter


        val layoutManager2 = LinearLayoutManager(requireContext())
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL)
        mBinding?.writeRecyclerView?.layoutManager = layoutManager2
        mBinding?.writeRecyclerView?.adapter = writeAdapter
    }

    override fun onDestroyView() {
        subscribeJob?.cancel()
        super.onDestroyView()
    }


}