package com.savent.recognition.face.domain.usecase

import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.savent.recognition.face.AppConstants
import com.savent.recognition.face.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationRequestUseCase {

    operator fun invoke(activity: Activity): Flow<Resource<Int>> {

        val locationRequest = LocationRequest.create()
            .setInterval(1000)
            .setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val locationServices = LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())

        return callbackFlow {
            val successListener =
                locationServices.addOnSuccessListener(activity) { response: LocationSettingsResponse? ->
                    launch {send(Resource.Success<Int>())}
                }
            val failSuccessListener =
                locationServices.addOnFailureListener(activity) { ex ->
                    launch {send(Resource.Error<Int>())}
                    if (ex is ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            val resolvable = ex as ResolvableApiException
                            resolvable.startResolutionForResult(
                                activity,
                                AppConstants.REQUEST_LOCATION_CODE
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            awaitClose {  }
        }
    }
}