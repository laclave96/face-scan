package com.savent.recognition.face.utils

import com.savent.recognition.face.utils.DateFormat

class IsFromToday() {

    companion object {
        operator fun invoke(timestamp: Long): Boolean {
            val today = DateFormat.getString(System.currentTimeMillis(), "yyyy-MM-dd")
            val currentDate = DateFormat.getString(timestamp, "yyyy-MM-dd")
            return currentDate == today
        }

    }


}