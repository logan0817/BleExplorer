package com.logan.framework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.logan.framework.interfaces.MultiItemEntity

/**
 * @author logan.gan
 * @date   2024/04/15
 * @desc   多种类目Adapter
 */
abstract class BaseMultiItemAdapter<T : MultiItemEntity> : BaseRecyclerViewAdapter<T, ViewBinding>() {

    /**
     * model需要实现MultiItemEntity接口
     */
    override fun getDefItemViewType(position: Int): Int {
        return getData()[position].itemType
    }

    /**
     * 如果需要实现多种类型子类ViewHolder，可以重写该方法
     */
    override fun onCreateDefViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return BaseBindViewHolder(getViewBinding(layoutInflater, parent, viewType))
    }
}