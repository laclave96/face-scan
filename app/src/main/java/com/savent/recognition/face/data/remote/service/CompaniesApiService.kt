package com.savent.recognition.face.data.remote.service

import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.data.remote.model.Company
import retrofit2.Response
import retrofit2.http.GET

interface CompaniesApiService {

    @GET(AppConstants.COMPANIES_API_PATH)
    suspend fun getCompanies(): Response<List<Company>>

}