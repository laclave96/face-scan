package com.savent.recognition.face.domain.usecase

import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class GetSavedPersonsUseCase(private val employeeRepository: EmployeeRepository) {

    operator fun invoke(query: String = ""): Flow<Resource<List<EmployeeEntity>>> = flow {
        employeeRepository.getEmployees(query).onEach { emit(it) }.collect()
    }
}