package com.savent.recognition.face.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.savent.recognition.face.data.local.datasource.LastDetectedPersonsLocalDataSource
import com.savent.recognition.face.data.remote.model.Presence
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.utils.DateFormat
import com.savent.recognition.face.utils.DateTimeObj
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.first

class GetPresencesUseCase(
    private val lastDetectedPersonsLocalDataSource: LastDetectedPersonsLocalDataSource,
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(companyId: Int, latLng: LatLng, place: String): Resource<List<Presence>> {
        lastDetectedPersonsLocalDataSource.getPersons().first().let {
            if (it.data is Resource.Error<*> || it.data == null)
                return Resource.Error(it.resId, it.message)

            val persons = employeeRepository.getEmployees(*(it.data.map { it1-> it1.localId }.toIntArray()))
            if (persons is Resource.Error<*> || persons.data == null)
                return Resource.Error(it.resId, it.message)

            return Resource.Success(persons.data.map { person ->
                Presence(
                    person.remoteId,
                    companyId,
                    DateTimeObj(
                        DateFormat.getString(System.currentTimeMillis(), "yyyy-MM-dd"),
                        DateFormat.getString(System.currentTimeMillis(), "HH:mm")
                    ),
                    latLng,
                    place,
                    null
                )
            })
        }
    }
}