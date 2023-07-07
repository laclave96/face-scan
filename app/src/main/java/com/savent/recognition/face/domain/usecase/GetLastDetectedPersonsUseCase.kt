package com.savent.recognition.face.domain.usecase

import com.savent.recognition.face.data.local.datasource.LastDetectedPersonsLocalDataSource
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.presentation.model.FaceData
import com.savent.recognition.face.utils.BitmapMapper
import com.savent.recognition.face.utils.NameFormat
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class GetLastDetectedPersonsUseCase(
    private val lastDetectedPersonsLocalDataSource: LastDetectedPersonsLocalDataSource,
    private val employeeRepository: EmployeeRepository
) {

    operator fun invoke(): Flow<Resource<List<DetectedFaceItem>>> = flow {
        lastDetectedPersonsLocalDataSource.getPersons().onEach {
            if (it.data is Resource.Error<*> || it.data == null) {
                emit(Resource.Error(it.resId, it.message))
                return@onEach
            }

            val persons =
                employeeRepository.getEmployees(*(it.data.map { it1 -> it1.localId }.toIntArray()))
            if (persons is Resource.Error<*> || persons.data == null) {
                emit(Resource.Error(it.resId, it.message))
                return@onEach
            }

            emit(Resource.Success(persons.data.map { person ->
                DetectedFaceItem(
                    person.id,
                    NameFormat.format("${person.name} ${person.paternalName} ${person.maternalName}"),
                    FaceData(
                        BitmapMapper.toBitmap(it.data.findLast { it1 -> it1.localId == person.id }?.faceBitmap
                        ?: ""
                    ), person.faceVectors[0]
                    )
                )
            }))
        }.collect()

    }
}