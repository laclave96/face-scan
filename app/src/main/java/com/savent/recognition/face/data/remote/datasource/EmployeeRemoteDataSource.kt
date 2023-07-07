package com.savent.recognition.face.data.remote.datasource

import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.data.remote.model.Employee
import com.savent.recognition.face.utils.Resource

interface EmployeeRemoteDataSource {

    suspend fun insertEmployeeFaceVector(
        companyId: Int,
        faceVectors: List<FloatArray>,
        credentials: Credentials
    ): Resource<Employee>

    suspend fun getEmployees(
        companyId: Int,
        facesVectors: List<FloatArray>
    ): Resource<List<Employee>>

}