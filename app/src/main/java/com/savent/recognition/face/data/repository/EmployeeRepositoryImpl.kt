package com.savent.recognition.face.data.repository

import com.savent.recognition.face.data.local.datasource.EmployeeLocalDataSource
import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.data.remote.datasource.EmployeeRemoteDataSource
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.data.remote.model.Employee
import com.savent.recognition.face.domain.repository.EmployeeRepository
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class EmployeeRepositoryImpl(
    private val localDataSource: EmployeeLocalDataSource,
    private val remoteDataSource: EmployeeRemoteDataSource,
    private val mapper: (Employee) -> EmployeeEntity
) : EmployeeRepository {

    override suspend fun insertEmployeesLocally(vararg employees: Employee): Resource<Int> =
        localDataSource.insertEmployees(*(employees.map { mapper(it) }.toTypedArray()))

    override suspend fun insertEmployeeFaceVector(
        companyId: Int,
        faceVectors: List<FloatArray>,
        credentials: Credentials
    ): Resource<Employee> =
        remoteDataSource.insertEmployeeFaceVector(companyId, faceVectors, credentials)

    override suspend fun getEmployees(vararg ids: Int): Resource<List<EmployeeEntity>> =
        localDataSource.getEmployees(*(ids))


    override fun getEmployees(query: String): Flow<Resource<List<EmployeeEntity>>> = flow {
        localDataSource.getEmployees(query).onEach { emit(it) }.collect()
    }

    override suspend fun fetchEmployees(
        companyId: Int,
        facesVectors: List<FloatArray>
    ): Resource<Int> {
        val response = remoteDataSource.getEmployees(companyId, facesVectors)
        if (response is Resource.Success) {
            response.data?.let {
                //Log.d("log_","response"+Gson().toJson(it))
                insertEmployeesLocally(*(it.toTypedArray()))
            }
            return Resource.Success()
        }
        return Resource.Error(resId = response.resId, message = response.message)
    }
}