package com.savent.recognition.face.utils

import android.content.Context
import com.savent.recognition.face.AppConstants

class IsLocationGranted {
    companion object{
        operator fun invoke(context: Context): Boolean =
            CheckPermissions.check(context, AppConstants.LOCATION_PERMISSIONS)
    }
}