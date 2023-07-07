package com.savent.recognition.face.data.local.database.dao

import androidx.room.*
import com.savent.recognition.face.data.local.model.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(vararg employees: EmployeeEntity):List<Long>

    @Update
    suspend fun updateEmployees(vararg employees: EmployeeEntity):Int

    @Query("SELECT * FROM persons WHERE remote_id =:remoteId")
    fun getEmployee(remoteId: Long): EmployeeEntity?

    @Query("SELECT * FROM persons WHERE id IN(:ids)")
    suspend fun getEmployees(vararg ids:Int): List<EmployeeEntity>?

    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%' OR rfc LIKE '%' || :query || '%' ORDER BY name ASC")
    fun getEmployees(query: String): Flow<List<EmployeeEntity>?>

    @Query("DELETE FROM persons WHERE id IN(:ids)")
    suspend fun delete(vararg ids: Int):Int

    @Query("DELETE FROM persons")
    suspend fun deleteAll():Int


}