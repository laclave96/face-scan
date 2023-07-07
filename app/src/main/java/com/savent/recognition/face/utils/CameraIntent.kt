package com.savent.recognition.face.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.savent.recognition.face.AppConstants
import java.io.File

class CameraIntent {

    companion object {
        fun start(activity: Activity): Uri? {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity.packageManager) != null) {

                val file = File(activity.filesDir, "face_capture.jpg")
                val imageUri = FileProvider.getUriForFile(
                    activity,
                    "com.savent.recognition.face.fileprovider",
                    file
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                //takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING",1)
                activity.startActivityForResult(
                    takePictureIntent,
                    AppConstants.REQUEST_IMAGE_CAPTURE
                )
                return imageUri
            }
            return null
        }
    }
}