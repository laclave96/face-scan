package com.savent.recognition.face.domain.usecase

import com.savent.recognition.face.data.local.datasource.LastDetectedPersonsLocalDataSource
import com.savent.recognition.face.presentation.model.DetectedFaceItem
import com.savent.recognition.face.presentation.model.PersonData
import com.savent.recognition.face.utils.BitmapMapper
import com.savent.recognition.face.utils.Resource


class SaveLocallyDetectedPersonsUseCase(
    private val lastDetectedPersonsLocalDataSource: LastDetectedPersonsLocalDataSource
) {

    suspend operator fun invoke(facesDetected: List<DetectedFaceItem>): Resource<Int> =
        lastDetectedPersonsLocalDataSource.insertOrUpdatePersons(facesDetected.map {
            PersonData(
                BitmapMapper.toString(it.faceData.bitmap),
                it.localId?:0
            )
        })

}