package com.savent.recognition.face.domain.repository

import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.data.remote.model.Employee
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {

    suspend fun insertEmployeesLocally(vararg employees: Employee): Resource<Int>

    suspend fun insertEmployeeFaceVector(
        companyId: Int,
        faceVectors: List<FloatArray>,
        credentials: Credentials
    ): Resource<Employee>

    suspend fun getEmployees(vararg ids: Int): Resource<List<EmployeeEntity>>

    fun getEmployees(query: String): Flow<Resource<List<EmployeeEntity>>>

    suspend fun fetchEmployees(companyId: Int, facesVectors: List<FloatArray>): Resource<Int>
}