package com.savent.recognition.face.utils

import com.savent.recognition.face.data.local.model.CompanyEntity
import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.data.remote.model.Company
import com.savent.recognition.face.data.remote.model.Employee

class Mappers {

    companion object{
        fun getPersonMapper(employee: Employee): EmployeeEntity{
            return EmployeeEntity(
                employee.id,
                employee.name,
                employee.maternal?:"",
                employee.paternal?:"",
                employee.rfc,
                employee.companyId,
                employee.faceVectors.toTypedArray()
            )
        }

        fun getCompanyMapper(company: Company): CompanyEntity{
            return CompanyEntity(
                company.id,
                company.name
            )
        }

    }

}