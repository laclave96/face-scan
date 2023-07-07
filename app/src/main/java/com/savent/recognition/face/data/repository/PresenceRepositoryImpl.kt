package com.savent.recognition.face.data.repository

import com.savent.recognition.face.data.remote.datasource.PresenceRemoteDataSource
import com.savent.recognition.face.data.remote.model.Presence
import com.savent.recognition.face.domain.repository.PresenceRepository
import com.savent.recognition.face.utils.Resource

class PresenceRepositoryImpl(private val remoteDataSource: PresenceRemoteDataSource): PresenceRepository {
    override suspend fun insertPresences(vararg presences: Presence): Resource<List<Long>> =
        remoteDataSource.insertPresences(*(presences))
}