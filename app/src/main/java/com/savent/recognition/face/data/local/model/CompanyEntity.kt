package com.savent.recognition.face.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @ColumnInfo(name = "remote_id")
    val remoteId: Int,
    val name: String,
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}