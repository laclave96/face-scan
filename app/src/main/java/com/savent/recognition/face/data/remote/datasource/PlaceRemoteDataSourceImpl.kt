package com.savent.recognition.face.data.remote.datasource

import android.util.Log
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Place
import com.savent.recognition.face.data.remote.service.PlaceApiService
import com.savent.recognition.face.utils.Resource

class PlaceRemoteDataSourceImpl(private val placeApiService: PlaceApiService): PlaceRemoteDataSource {

    override suspend fun getPlace(latitude: String, longitude: String): Resource<Place> {
        try {
            val response = placeApiService.getPlace(latitude,longitude)
            //Log.d("log_",response.toString()+response.errorBody().toString())
            if (response.isSuccessful)
                return Resource.Success(response.body())
            return Resource.Error(resId = R.string.get_place_error)
        } catch (e: Exception) {
            //Log.d("log_",e.toString())
            return Resource.Error(message = "Error al conectar")
        }
    }
}