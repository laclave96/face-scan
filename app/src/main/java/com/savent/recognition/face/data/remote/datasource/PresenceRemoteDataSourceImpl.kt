package com.savent.recognition.face.data.remote.datasource

import android.util.Log
import com.google.gson.Gson
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Presence
import com.savent.recognition.face.data.remote.service.PresencesApiService
import com.savent.recognition.face.utils.Resource

class PresenceRemoteDataSourceImpl(private val presenceApiService: PresencesApiService) :
    PresenceRemoteDataSource {
    override suspend fun insertPresences(vararg presences: Presence): Resource<List<Long>> {
        try {
            Log.d("log_",Gson().toJson(presences))
            val response = presenceApiService.insertPresences(Gson().toJson(presences))
            Log.d("log_",response.toString()+response.errorBody().toString())
            if (response.isSuccessful)
                return Resource.Success(response.body())
            return Resource.Error(resId = R.string.insert_presence_error)
        } catch (e: Exception) {
            Log.d("log_",e.toString())
            return Resource.Error(message = "Error al conectar")
        }
    }
}