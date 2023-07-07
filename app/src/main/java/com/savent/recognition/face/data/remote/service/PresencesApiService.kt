package com.savent.recognition.face.data.remote.service

import com.savent.recognition.face.AppConstants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface PresencesApiService {

    @POST(AppConstants.PRESENCES_API_PATH)
    suspend fun insertPresences(@Query("presences") presences: String): Response<List<Long>>

}