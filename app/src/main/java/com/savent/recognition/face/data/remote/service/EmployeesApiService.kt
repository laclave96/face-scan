package com.savent.recognition.face.data.remote.service

import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.data.remote.model.Credentials
import com.savent.recognition.face.data.remote.model.Employee
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EmployeesApiService {

    @GET(AppConstants.EMPLOYEEES_API_PATH)
    suspend fun getEmployee(
        @Query("companyId") companyId: Int,
        @Query("facesVectors") facesVectors: String,
    ): Response<List<Employee>>

    @POST(AppConstants.EMPLOYEEES_API_PATH)
    suspend fun insertEmployeeFaceVector(
        @Query("companyId") companyId: Int,
        @Query("facesVectors") faceVectors: String,
        @Query("credentials") credentials: String,
    ): Response<Employee>


}