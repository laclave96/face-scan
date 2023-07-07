package com.savent.recognition.face.domain.repository

import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.data.remote.model.Company
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {

    suspend fun insertCompanies(vararg companies: Company): Resource<Int>

    suspend fun getCompany(id: Int): Resource<CompanyEntity>

    suspend fun getCompanyByRemoteId(remoteId: Int): Resource<CompanyEntity>

    suspend fun getCompanies(): Resource<List<CompanyEntity>>

    fun getCompanies(query: String): Flow<Resource<List<CompanyEntity>>>

    suspend fun fetchCompanies(): Resource<Int>

}