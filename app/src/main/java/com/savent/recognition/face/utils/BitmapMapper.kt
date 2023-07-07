package com.savent.recognition.face.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class BitmapMapper {

    companion object {

        fun toString(bitmap: Bitmap): String {
            val COMPRESSION_QUALITY = 100
            val encodedImage: String
            val byteArrayBitmapStream = ByteArrayOutputStream()
            bitmap.compress(
                Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream
            )
            val b: ByteArray = byteArrayBitmapStream.toByteArray()
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT)
            return encodedImage
        }

        fun toBitmap(str: String): Bitmap {
            val decodedString: ByteArray = Base64.decode(str, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }

}