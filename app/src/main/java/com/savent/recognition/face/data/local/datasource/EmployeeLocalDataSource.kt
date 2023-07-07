package com.savent.recognition.face.data.local.datasource

import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow

abstract class EmployeeLocalDataSource {

    abstract suspend fun insertEmployees(vararg employees: EmployeeEntity): Resource<Int>

    abstract suspend fun updateEmployees(vararg employees: EmployeeEntity): Resource<Int>

    abstract suspend fun getEmployees(vararg ids: Int): Resource<List<EmployeeEntity>>

    abstract fun getEmployees(query: String): Flow<Resource<List<EmployeeEntity>>>

    protected fun alreadyExists(employee: EmployeeEntity, oldData: List<EmployeeEntity>): Boolean {
        oldData.forEach {
            if (employee.remoteId == it.remoteId) return true
        }
        return false
    }

    protected fun areContentsTheSame(employee: EmployeeEntity, oldData: List<EmployeeEntity>): Boolean {
        oldData.forEach {
            if (employee.hashCode() == it.hashCode()) return true
        }
        return false
    }

    protected fun prepareDataToUpdate(
        newData: List<EmployeeEntity>,
        oldData: List<EmployeeEntity>
    ): List<EmployeeEntity> {
        newData.forEach { newEmployee ->
            oldData.forEach { oldEmployee ->
                if (newEmployee.remoteId == oldEmployee.remoteId)
                    newEmployee.id = oldEmployee.id
            }
        }
        return newData
    }

}