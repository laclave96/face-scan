package com.savent.recognition.face.data.remote.datasource

import android.util.Log
import com.savent.recognition.face.R
import com.savent.recognition.face.data.remote.model.Company
import com.savent.recognition.face.data.remote.service.CompaniesApiService
import com.savent.recognition.face.utils.Resource

class CompanyRemoteDataSourceImpl(private val companiesApiService: CompaniesApiService) :
    CompanyRemoteDataSource {

    override suspend fun getCompanies(): Resource<List<Company>> {
        try {
            val response = companiesApiService.getCompanies()
            //Log.d("log_",response.toString()+response.errorBody().toString())
            if (response.isSuccessful)
                return Resource.Success(response.body())
            return Resource.Error(resId = R.string.sync_local_data_error)
        } catch (e: Exception) {
            //Log.d("log_",e.toString())
            return Resource.Error(message = "Error al conectar")
        }
    }
}