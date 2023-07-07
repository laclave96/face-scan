package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.data.local.model.AppPreferences
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class AppPreferencesLocalDatasourceImpl(
    private val dataObjectStorage: DataObjectStorage<AppPreferences>
): AppPreferencesLocalDatasource {
    override suspend fun insertAppPreferences(preferences: AppPreferences): Resource<Int> =
        dataObjectStorage.saveData(preferences)


    override fun getAppPreferences(): Flow<Resource<AppPreferences>> = flow  {
        dataObjectStorage.getData().onEach { emit(it) }.collect()
    }

    override suspend fun updateAppPreferences(preferences: AppPreferences): Resource<Int> =
        dataObjectStorage.saveData(preferences)

    override suspend fun deleteAppPreferences(): Resource<Int> =
        dataObjectStorage.clear()
}