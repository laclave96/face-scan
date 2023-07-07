package com.savent.recognition.face.domain.usecase

import com.savent.recognition.face.data.local.model.EmployeeEntity
import com.savent.recognition.face.utils.Resource

class GetPersonIfExistsLocallyUseCase {

    operator fun invoke(companyId: Int, faceVector: FloatArray, persons: Resource<List<EmployeeEntity>>): EmployeeEntity? {
        val threshold = 0.56F//equal confidence value = 78%
        var localEmployee: EmployeeEntity? = null
        persons.let {
            if (it is Resource.Error || it.data == null) return null
            var max = threshold

            it.data.filter {it1-> companyId == it1.companyId }.forEach { entity ->
                entity.faceVectors.forEach { featureVector ->
                    val similarity = computeCosineSimilarityLoss(faceVector, featureVector)
                    if (similarity > max) {
                        max = similarity
                        localEmployee = entity
                    }

                }

            }
        }
        return localEmployee

    }

    fun computeCosineSimilarityLoss(A: FloatArray?, B: FloatArray?): Float {
        if (A == null || B == null || A.isEmpty() || B.isEmpty() || A.size != B.size) {
            return -1.0f
        }
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in A.indices) {
            dotProduct += (A[i] * B[i]).toDouble()
            normA += (A[i] * A[i]).toDouble()
            normB += (B[i] * B[i]).toDouble()
        }
        return if (normA == 0.0 && normB == 0.0) {
            -1.0f
        } else (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))).toFloat()
    }

}