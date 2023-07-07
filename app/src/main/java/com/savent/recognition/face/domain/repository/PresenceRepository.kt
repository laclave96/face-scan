package com.savent.recognition.face.domain.repository

import com.savent.recognition.face.data.remote.model.Presence
import com.savent.recognition.face.utils.Resource

interface PresenceRepository {
    suspend fun insertPresences(vararg presences: Presence): Resource<List<Long>>
}