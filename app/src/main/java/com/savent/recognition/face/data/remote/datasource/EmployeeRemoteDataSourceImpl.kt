package com.savent.recognition.face.data.remote.datasource

import android.util.Log
import com.google.gson.Gson
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.data.remote.model.Employee
import com.savent.recognition.face.data.remote.service.EmployeesApiService
import com.savent.recognition.face.utils.Resource

class EmployeeRemoteDataSourceImpl(private val employeesApiService: EmployeesApiService) :
    EmployeeRemoteDataSource {

    override suspend fun insertEmployeeFaceVector(
        companyId: Int,
        faceVectors: List<FloatArray>,
        credentials: Credentials
    ): Resource<Employee> {
        try {
            val response = employeesApiService.insertEmployeeFaceVector(
                companyId,
                Gson().toJson(faceVectors),
                credentials.toString()
            )
            //Log.d("log_",response.toString())
            //Log.d("log_",Gson().toJson(response.errorBody()))
            if (response.isSuccessful)
                return Resource.Success(response.body())
            return Resource.Error(resId = R.string.invalid_credentials)
        } catch (e: Exception) {
            //Log.d("log_",e.toString())
            return Resource.Error(message = "Error al conectar")
        }
    }

    override suspend fun getEmployees(
        companyId: Int,
        facesVectors: List<FloatArray>
    ): Resource<List<Employee>> {
        try {
            val response = employeesApiService.getEmployee(companyId, Gson().toJson(facesVectors))
            //Log.d("log_",response.toString()+response.errorBody().toString())
            if (response.isSuccessful)
                return Resource.Success(response.body())
            return Resource.Error(resId = R.string.get_remote_persons_error)
        } catch (e: Exception) {
            //Log.d("log_",e.toString())
            return Resource.Error(message = "Error al conectar")
        }
    }
}