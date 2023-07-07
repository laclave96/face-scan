package com.savent.recognition.face.data.local.database.dao

import androidx.room.*
import com.savent.recognition.face.data.local.model.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(vararg companies: CompanyEntity):List<Long>

    @Update
    suspend fun updateCompanies(vararg companies: CompanyEntity):Int

    @Query("SELECT * FROM companies WHERE id =:id")
    fun getCompany(id: Int): CompanyEntity?

    @Query("SELECT * FROM companies WHERE remote_id =:remoteId")
    fun getCompanyByRemoteId(remoteId: Int): CompanyEntity?

    @Query("SELECT * FROM companies")
    suspend fun getCompanies(): List<CompanyEntity>?

    @Query("SELECT * FROM companies WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun getCompanies(query: String): Flow<List<CompanyEntity>?>

    @Query("DELETE FROM companies WHERE id IN(:ids)")
    suspend fun delete(vararg ids: Int):Int

    @Query("DELETE FROM persons")
    suspend fun deleteAll():Int

}