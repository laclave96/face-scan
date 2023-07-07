package com.savent.recognition.face.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class CheckPermissions {

    companion object{
        fun check(context: Context?, permissions: Array<String>): Boolean {
            if (context != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}