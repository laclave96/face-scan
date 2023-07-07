package com.savent.recognition.face.utils

sealed class Resource<T>(val data: T? = null, val resId: Int? = null, val message: String? = null) {

    class Loading<T>() : Resource<T>()
    class Success<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(resId: Int? = null, message: String? = null) :
        Resource<T>(resId = resId, message = message)

}
