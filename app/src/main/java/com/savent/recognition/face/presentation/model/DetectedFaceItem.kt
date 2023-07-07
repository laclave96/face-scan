package com.savent.recognition.face.presentation.model

data class DetectedFaceItem(
    val localId: Int?,
    val name: String,
    val faceData: FaceData,
) {
    fun isUnknown(): Boolean = localId == null
}