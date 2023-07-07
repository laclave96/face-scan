package com.savent.recognition.face

import android.Manifest

object AppConstants {

    const val EMPTY_JSON_STRING = "[]"
    const val APP_PREFERENCES = "app_preferences"
    const val LAST_DETECTED_PERSONS = "last_detected_persons"
    const val SAVENT_FACE_RECOGNITION_API_BASE_URL =
        "your_api_url"
    const val APP_DATABASE_NAME = "app_database"
    const val AUTHORIZATION = "your_auth"
    const val EMPLOYEEES_API_PATH = "employees/"
    const val COMPANIES_API_PATH = "companies/"
    const val PRESENCES_API_PATH = "presences/"
    const val BIG_DATA_CLOUD_API_BASE_URL = "https://api.bigdatacloud.net/"
    const val REVERSE_GEOCODING_API_PATH = "data/reverse-geocode-client"
    const val PERMISSION_REQUESTS = 1
    const val REQUEST_IMAGE_CAPTURE = 1001

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    const val REQUEST_LOCATION_CODE = 8989
    const val REQUEST_LOCATION_PERMISSION_CODE = 10
}