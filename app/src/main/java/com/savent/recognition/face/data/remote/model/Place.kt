package com.savent.recognition.face.data.remote.model

class Place(
    val latitude: Float,
    val longitude: Float,
    val city: String,
    val countryName: String,
    val principalSubdivision: String
){
    override fun toString(): String = "$city, $principalSubdivision, $countryName"
}