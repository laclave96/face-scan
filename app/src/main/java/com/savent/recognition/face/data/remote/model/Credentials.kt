package com.savent.recognition.face.data.remote.model

import com.google.gson.Gson

class Credentials(val rfc: String, val pin: String) {

    override fun toString(): String = Gson().toJson(this)

}