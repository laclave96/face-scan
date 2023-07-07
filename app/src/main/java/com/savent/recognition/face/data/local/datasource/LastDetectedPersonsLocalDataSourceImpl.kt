package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.presentation.model.PersonData
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class LastDetectedPersonsLocalDataSourceImpl(
    private val dataObjectStorage: DataObjectStorage<List<PersonData>>
) : LastDetectedPersonsLocalDataSource {

    override suspend fun insertOrUpdatePersons(persons: List<PersonData>): Resource<Int> =
        dataObjectStorage.saveData(persons)

    override fun getPersons(): Flow<Resource<List<PersonData>>> = flow  {
        dataObjectStorage.getData().onEach { emit(it) }.collect()
    }


}