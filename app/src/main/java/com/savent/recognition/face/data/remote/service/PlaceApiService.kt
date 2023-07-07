package com.savent.recognition.face.data.remote.service

import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.data.remote.model.Place
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceApiService {

    @GET(AppConstants.REVERSE_GEOCODING_API_PATH)
    suspend fun getPlace(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("localityLanguage") language: String = "es",
    ): Response<Place>

}