package com.savent.recognition.face.utils

import java.text.SimpleDateFormat
import java.util.*

class DateFormat() {

    companion object {
        fun getString(timestamp: Long, format: String): String {
            val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
            return simpleDateFormat.format(Date(timestamp))
        }

    }


}