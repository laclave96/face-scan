package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AppPreferencesLocalDatasource {

    suspend fun insertAppPreferences(preferences: AppPreferences): Resource<Int>

    fun getAppPreferences(): Flow<Resource<AppPreferences>>

    suspend fun updateAppPreferences(preferences: AppPreferences): Resource<Int>

    suspend fun deleteAppPreferences(): Resource<Int>
}