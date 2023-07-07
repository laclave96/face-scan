package com.savent.recognition.face.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class EmployeeEntity(
    @ColumnInfo(name = "remote_id")
    val remoteId: Long,
    val name: String,
    @ColumnInfo(name = "maternal_name")
    val maternalName: String,
    @ColumnInfo(name = "paternal_name")
    val paternalName: String,
    val rfc: String,
    @ColumnInfo(name = "company_id")
    val companyId: Int,
    @ColumnInfo(name = "face_vectors")
    val faceVectors: Array<FloatArray>,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmployeeEntity

        if (remoteId != other.remoteId) return false
        if (name != other.name) return false
        if (maternalName != other.maternalName) return false
        if (paternalName != other.paternalName) return false
        if (rfc != other.rfc) return false
        if (companyId != other.companyId) return false
        if (!faceVectors.contentDeepEquals(other.faceVectors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remoteId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + maternalName.hashCode()
        result = 31 * result + paternalName.hashCode()
        result = 31 * result + rfc.hashCode()
        result = 31 * result + companyId
        result = 31 * result + faceVectors.contentDeepHashCode()
        return result
    }
}