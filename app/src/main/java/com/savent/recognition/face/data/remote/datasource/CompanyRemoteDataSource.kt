package com.savent.recognition.face.data.remote.datasource

import com.savent.recognition.face.data.remote.model.Company
import com.savent.recognition.face.utils.Resource

interface CompanyRemoteDataSource {
    suspend fun getCompanies(): Resource<List<Company>>
}