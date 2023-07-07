package com.savent.recognition.face.utils

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun toLatLng(latLng: String): LatLng =
        Gson().fromJson(latLng, object : TypeToken<LatLng>() {}.type)

    @TypeConverter
    fun fromLatLng(latLng: LatLng): String = Gson().toJson(latLng)

    @TypeConverter
    fun toVectorList(vectorList: String): Array<FloatArray> =
        Gson().fromJson(vectorList, object : TypeToken<Array<FloatArray>>() {}.type)

    @TypeConverter
    fun fromVectorList(vectorList: Array<FloatArray>): String = Gson().toJson(vectorList)

}