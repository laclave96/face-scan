package com.savent.recognition.face.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.savent.recognition.face.data.remote.datasource.PlaceRemoteDataSource
import com.savent.recognition.face.utils.Resource

class GetPlaceByCoordinatesUseCase(private val placeRemoteDataSource: PlaceRemoteDataSource) {

    suspend operator fun invoke(latLng: LatLng): Resource<String> {

        placeRemoteDataSource.getPlace(latLng.latitude.toString(), latLng.longitude.toString()).let {
            return if (it is Resource.Success && it.data != null) Resource.Success(it.data.toString())
            else Resource.Error(it.resId,it.message)
        }

    }
}