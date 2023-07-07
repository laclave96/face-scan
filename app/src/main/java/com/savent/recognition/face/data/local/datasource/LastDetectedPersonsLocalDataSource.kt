package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.presentation.model.PersonData
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow

interface LastDetectedPersonsLocalDataSource {

    suspend fun insertOrUpdatePersons(persons: List<PersonData>): Resource<Int>

    fun getPersons(): Flow<Resource<List<PersonData>>>

}