package com.savent.recognition.face.data.remote.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.savent.recognition.face.utils.DateTimeObj

data class Presence(
    @SerializedName("employee_id")
    val employeeId: Long,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("date_timestamp")
    val dateTimestamp: DateTimeObj,
    val location: LatLng,
    val place: String,
    val reason: String?,
) {
}