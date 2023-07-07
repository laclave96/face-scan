package com.savent.recognition.face.data.remote.model

import com.google.gson.annotations.SerializedName

data class Employee(
    val id: Long,
    val name: String,
    val maternal: String?,
    val paternal: String?,
    val rfc: String,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("face_vectors")
    val faceVectors: List<FloatArray>
){
}