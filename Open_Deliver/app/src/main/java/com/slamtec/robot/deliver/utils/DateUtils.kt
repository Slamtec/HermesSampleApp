package com.slamtec.robot.deliver.utils

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(time: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss")
        return dateFormat.format(Date(time))
    }

    fun formatDateSeconds(time: Long): String {
        val dateFormat = SimpleDateFormat("ss")
        return dateFormat.format(Date(time))
    }



    fun string2Date(string: String?): Date? {
        var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")
        try {
            return format.parse(string)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return null
    }

}