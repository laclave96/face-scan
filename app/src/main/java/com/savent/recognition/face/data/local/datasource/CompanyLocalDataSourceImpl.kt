package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.database.dao.CompanyDao
import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class CompanyLocalDataSourceImpl(private val companyDao: CompanyDao) : CompanyLocalDataSource() {
    override suspend fun insertCompanies(vararg companies: CompanyEntity): Resource<Int> {
        val oldData = companyDao.getCompanies()?.let { it }
            ?: return Resource.Error(R.string.get_companies_error)
        val newCompanies = companies.filter { !areContentsTheSame(it, oldData) }
        val toInsert = newCompanies.filter { !alreadyExists(it, oldData) }
        val toUpdate = prepareDataToUpdate(newCompanies.minus(toInsert), oldData)
        val result1 = companyDao.insertCompanies(*(toInsert.toTypedArray()))
        val result2 = companyDao.updateCompanies(*(toUpdate.toTypedArray()))
        if (result1.size != toInsert.size && result2 != toUpdate.size)
            return Resource.Error(R.string.insert_companies_error)
        return Resource.Success()
    }

    override suspend fun updateCompanies(vararg companies: CompanyEntity): Resource<Int> {
        val result = companyDao.updateCompanies(*(companies))
        if (result == 0) return Resource.Error(R.string.update_companies_error)
        return Resource.Success()
    }

    override suspend fun getCompany(id: Int): Resource<CompanyEntity> =
        companyDao.getCompany(id)?.let { Resource.Success(it) }
            ?: Resource.Error(R.string.get_companies_error)


    override suspend fun getCompanyByRemoteId(remoteId: Int): Resource<CompanyEntity> =
        companyDao.getCompany(remoteId)?.let { Resource.Success(it) }
            ?: Resource.Error(R.string.get_companies_error)

    override suspend fun getCompanies(): Resource<List<CompanyEntity>> =
        companyDao.getCompanies()?.let { Resource.Success(it) }
            ?: Resource.Error(R.string.get_companies_error)


    override fun getCompanies(query: String): Flow<Resource<List<CompanyEntity>>> = flow {
        companyDao.getCompanies(query).onEach {
            it?.let { it1 -> emit(Resource.Success(it1)) }
                ?: emit(Resource.Error<List<CompanyEntity>>(R.string.get_companies_error))
        }.collect()
    }
}