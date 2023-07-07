package com.savent.recognition.face.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.savent.recognition.face.data.local.model.CompanyEntity


class CompaniesDiffCallBack(
    private val oldList: List<CompanyEntity>, private val newList: List<CompanyEntity>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].hashCode() == newList[newItemPosition].hashCode()

    }

}