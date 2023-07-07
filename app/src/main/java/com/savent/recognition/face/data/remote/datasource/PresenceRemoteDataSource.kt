package com.savent.recognition.face.data.remote.datasource

import com.savent.recognition.face.data.remote.model.Presence
import com.savent.recognition.face.utils.Resource

interface PresenceRemoteDataSource{
    suspend fun insertPresences(vararg presences: Presence): Resource<List<Long>>

}