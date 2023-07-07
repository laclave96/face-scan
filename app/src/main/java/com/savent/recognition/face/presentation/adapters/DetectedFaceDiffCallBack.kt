package com.savent.recognition.face.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.savent.recognition.face.presentation.model.DetectedFaceItem

class DetectedFaceDiffCallBack(
    private val oldList: List<DetectedFaceItem>, private val newList: List<DetectedFaceItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].faceData.featureVector.hashCode() ==
                newList[newItemPosition].faceData.featureVector.hashCode()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].hashCode() == newList[newItemPosition].hashCode()

    }

}