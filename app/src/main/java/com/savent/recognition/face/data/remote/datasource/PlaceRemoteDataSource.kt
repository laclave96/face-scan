package com.savent.recognition.face.data.remote.datasource

import com.savent.recognition.face.data.remote.model.Place
import com.savent.recognition.face.utils.Resource

interface PlaceRemoteDataSource {

    suspend fun getPlace(latitude: String, longitude: String): Resource<Place>
}