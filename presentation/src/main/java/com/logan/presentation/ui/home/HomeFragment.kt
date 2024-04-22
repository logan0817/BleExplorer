package com.logan.presentation.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.ScanResult
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.logan.framework.adapter.BaseBindViewHolder
import com.logan.framework.adapter.BaseRecyclerViewAdapter
import com.logan.framework.ext.onClick
import com.logan.framework.toast.TipsToast
import com.logan.framework.utils.AppExit
import com.logan.presentation.R
import com.logan.presentation.base.BaseBluetoothMvvmFragment
import com.logan.presentation.databinding.FragmentHomeBinding
import com.logan.presentation.databinding.FragmentHomeBleDeviceResultBinding
import com.logan.presentation.helper.BleManager
import com.logan.presentation.ui.home.viewmodel.HomeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   home page
 */
class HomeFragment : BaseBluetoothMvvmFragment<FragmentHomeBinding, HomeViewModel>() {
    private var scanJob: Job? = null

    override var handleBackPressedEnable = true
    override fun onBackPressed() {
        AppExit.onBackPressed(
            requireActivity(),
            { TipsToast.showTips(getString(R.string.app_exit_one_more_press)) }
        )
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        initSearchView()
        initRecyclerView()
        initListeners()
    }

    private fun initSearchView() {
        mBinding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterText = query
                val filterList = BleManager.scanDevides.filter { getDeviceCheckFilterRule(it) }
                scanAdapter.setData(filterList)
                return true;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterText = newText
                val filterList = BleManager.scanDevides.filter { getDeviceCheckFilterRule(it) }
                scanAdapter.setData(filterList)
                return true;
            }
        })
    }

    private fun getDeviceCheckFilterRule(scanResult: ScanResult): Boolean {
        return if (filterText.isNullOrBlank()) {
            true
        } else {
            scanResult.device.name?.contains(filterText!!, true) == true
                    || scanResult.deviceAddress.address.contains(filterText!!, true)
        }
    }

    var filterText: String? = null

    val scanAdapter =
        object : BaseRecyclerViewAdapter<ScanResult, FragmentHomeBleDeviceResultBinding>() {
            override fun onBindDefViewHolder(
                holder: BaseBindViewHolder<FragmentHomeBleDeviceResultBinding>,
                item: ScanResult?,
                position: Int
            ) {
                holder.binding.tvRssi.text = "${item?.rssi}dBm"
                holder.binding.tvName.text = item?.device?.name
                holder.binding.tvMac.text = item?.deviceAddress?.address
                holder.binding.signalView.setSignalValue(getValueByRssi(item?.rssi ?: 0))
                holder.binding.signalView.setSignalTypeText("")
                if (filterText.isNullOrBlank()
                    || item?.device?.name?.contains(filterText!!, true) == true
                    || item?.deviceAddress?.address?.contains(filterText!!, true) == true
                ) {
                    holder.itemView.visibility = View.VISIBLE
                } else {
                    holder.itemView.visibility = View.GONE
                }
            }

            override fun getViewBinding(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): FragmentHomeBleDeviceResultBinding {
                return FragmentHomeBleDeviceResultBinding.inflate(layoutInflater, parent, false)
            }
        }

    private fun getValueByRssi(rssi: Int): Int {
        return if (rssi > -60) {
            5
        } else if (rssi > -65) {
            4
        } else if (rssi > -70) {
            3
        } else if (rssi > -75) {
            2
        } else if (rssi > -80) {
            1
        } else {
            0
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        mBinding?.recyclerView?.layoutManager = layoutManager
        mBinding?.recyclerView?.adapter = scanAdapter
        scanAdapter.onItemClickListener = { view, position ->
            val scanResult = scanAdapter.getData()[position]

            findNavController().navigate(
                HomeFragmentDirections.actionHomefragmentToBluetoothdevicefragment(
                    scanResult.deviceAddress.address,
                    scanResult.device.name ?: ""
                )
            )
        }
    }


    private fun initListeners() {
        mBinding?.refreshLayout?.apply {
            setEnableRefresh(true)
            setEnableLoadMore(false)
            setOnRefreshListener {
                mBinding?.refreshLayout?.finishRefresh(800)
                scanAdapter.clear()
                doPermissionsCheck {
                    startScanning()
                }
            }
            autoRefresh()
        }
        mBinding?.ivSort?.onClick {
            val datas = scanAdapter.getData()
            datas.sortByDescending { it.rssi }
            scanAdapter.notifyDataSetChanged()
            mBinding?.recyclerView?.scrollToPosition(0)
        }
        mBinding?.ivSetup?.onClick {

            val deviceLanguage = Locale.getDefault().language
            val url = if (deviceLanguage == "zh") {
                "https://blog.csdn.net/notwalnut/article/details/138086881" // Chinese language
            } else {
                "https://github.com/logan0817/BleExplorer" // Other languages
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        BleManager.disconnectAllDevice()
        stopScanning()
        scanJob = mViewModel.viewModelScope.launch {
            try {
                val bluetoothLe = BluetoothLe(requireContext())
                val scanResultFlow = bluetoothLe.scan()
                scanResultFlow.collect { result ->
                    if (!result.device.name.isNullOrBlank()) {
                        processingResultsAndFiltering(result)
                    }
                }
            } catch (exception: Exception) {
                // handle connection errors
            }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    fun processingResultsAndFiltering(result: ScanResult) {
        //Record all scan results
        val scanDevidesIndex =
            BleManager.scanDevides.indexOfFirst { it.deviceAddress.address == result.deviceAddress.address }
        if (scanDevidesIndex != -1) {
            BleManager.scanDevides[scanDevidesIndex] = result
        } else {
            BleManager.scanDevides.add(result)
        }

        //Processing adapter results
        if (getDeviceCheckFilterRule(result)) {
            val scanAdapterIndex = scanAdapter.getData()
                .indexOfFirst { it.deviceAddress.address == result.deviceAddress.address }
            if (scanAdapterIndex != -1) {
                scanAdapter.updateItem(scanAdapterIndex, result)
                scanAdapter.notifyItemChanged(scanAdapterIndex)
                Log.i(
                    TAG,
                    "scanResultFlow 10001: NAME:${result.device.name}  MAC:${result.deviceAddress.address} scanAdapterIndex=$scanAdapterIndex"
                )
            } else {
                scanAdapter.addItem(result)
                Log.i(
                    TAG,
                    "scanResultFlow 10002: NAME:${result.device.name}  MAC:${result.deviceAddress.address}"
                )
            }
        }
    }

    override fun onDestroyView() {
        stopScanning()
        super.onDestroyView()
    }

    private companion object {
        private const val TAG = "HomeFragment"
    }
}