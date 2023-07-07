package com.savent.recognition.face.data.repository

import com.savent.recognition.face.data.local.datasource.CompanyLocalDataSource
import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.data.remote.datasource.CompanyRemoteDataSource
import com.savent.recognition.face.data.remote.model.Company
import com.savent.recognition.face.domain.repository.CompanyRepository
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class CompanyRepositoryImpl(
    private val localDataSource: CompanyLocalDataSource,
    private val remoteDataSource: CompanyRemoteDataSource,
    private val mapper: (Company) -> CompanyEntity
) : CompanyRepository {
    override suspend fun insertCompanies(vararg companies: Company): Resource<Int> =
        localDataSource.insertCompanies(*(companies.map { mapper(it) }.toTypedArray()))

    override suspend fun getCompany(id: Int): Resource<CompanyEntity> =
        localDataSource.getCompany(id)

    override suspend fun getCompanyByRemoteId(remoteId: Int): Resource<CompanyEntity> =
        localDataSource.getCompanyByRemoteId(remoteId)

    override suspend fun getCompanies(): Resource<List<CompanyEntity>> =
        localDataSource.getCompanies()

    override fun getCompanies(query: String): Flow<Resource<List<CompanyEntity>>> = flow{
        localDataSource.getCompanies(query).onEach { emit(it) }.collect()
    }

    override suspend fun fetchCompanies(): Resource<Int> {
        val response = remoteDataSource.getCompanies()
        if (response is Resource.Success) {
            response.data?.let {
                //Log.d("log_","response"+Gson().toJson(it))
                insertCompanies(*(it.toTypedArray()))
            }
            return Resource.Success()
        }
        return Resource.Error(resId = response.resId, message = response.message)
    }
}