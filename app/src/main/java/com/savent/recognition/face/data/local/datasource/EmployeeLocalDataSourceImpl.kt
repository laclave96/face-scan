package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.R
import com.savent.recognition.face.data.local.database.dao.EmployeeDao
import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class EmployeeLocalDataSourceImpl(private val employeeDao: EmployeeDao) : EmployeeLocalDataSource(){

    override suspend fun insertEmployees(vararg employees: EmployeeEntity): Resource<Int> {
        val oldData = employeeDao.getEmployees()?.let { it }
            ?: return Resource.Error(R.string.get_persons_error)
        val newPersons = employees.filter { !areContentsTheSame(it, oldData) }
        val toInsert = newPersons.filter { !alreadyExists(it, oldData) }
        val toUpdate = prepareDataToUpdate(newPersons.minus(toInsert), oldData)
        val result1 = employeeDao.insertEmployees(*(toInsert.toTypedArray()))
        val result2 = employeeDao.updateEmployees(*(toUpdate.toTypedArray()))
        if (result1.size != toInsert.size && result2 != toUpdate.size)
            return Resource.Error(R.string.insert_persons_error)
        return Resource.Success()
    }

    override suspend fun updateEmployees(vararg employees: EmployeeEntity): Resource<Int> {
        val result = employeeDao.updateEmployees(*(employees))
        if (result == 0) return Resource.Error(R.string.update_persons_error)
        return Resource.Success()
    }

    override suspend fun getEmployees(vararg ids: Int): Resource<List<EmployeeEntity>> =
        employeeDao.getEmployees(*(ids))?.let { Resource.Success(it) }
            ?: Resource.Error(R.string.get_persons_error)


    override fun getEmployees(query: String): Flow<Resource<List<EmployeeEntity>>> = flow {
        employeeDao.getEmployees(query).onEach {
            it?.let { it1 -> emit(Resource.Success(it1)) }
                ?: emit(Resource.Error<List<EmployeeEntity>>(R.string.get_persons_error))
        }.collect()
    }


}