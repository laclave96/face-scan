package com.savent.recognition.face.domain.usecase

import android.util.Log
import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.presentation.model.FaceData
import com.savent.recognition.face.utils.NameFormat
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class GetDetectedFacesUseCase(
    private val getSavedPersonsUseCase: GetSavedPersonsUseCase,
    private val getPersonIfExistsLocallyUseCase: GetPersonIfExistsLocallyUseCase
) {

    suspend operator fun invoke(companyId: Int, dataFaces: Flow<List<FaceData>>):
            Flow<Resource<List<DetectedFaceItem>>> =
        flow {
            dataFaces.combine(getSavedPersonsUseCase()) { vectors, persons ->
                val detectedFaces = ArrayList<DetectedFaceItem>(vectors.size)
                vectors.forEach {
                    val person =
                        getPersonIfExistsLocallyUseCase(companyId, it.featureVector, persons)
                    val name = "${person?.name} ${person?.paternalName} ${person?.maternalName}"

                    detectedFaces.add(
                        DetectedFaceItem(
                            person?.id,
                            NameFormat.format(name),
                            it
                        )
                    )
                }

                emit(Resource.Success(detectedFaces.toList()))

            }.collect()
        }

}