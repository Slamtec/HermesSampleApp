package com.slamtec.fooddelivery.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatDateT(time: Long): String {
        var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date(time))
    }

    fun formatToMS(time: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss")
        return dateFormat.format(Date(time))
    }

    fun formatToWeek(time: Long): String {
        val dateFormat = SimpleDateFormat("EEEE", Locale.CHINESE)
        return dateFormat.format(Date(time))
    }

    fun formatToDay(time: Long): String {
        val dateFormat = SimpleDateFormat("MMMd日", Locale.CHINESE)
        return dateFormat.format(Date(time))
    }

    fun formatToTime(time: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)
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


    fun getZeroClockDate(): String {
        var c = Calendar.getInstance()
        c.time = Date()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")
        return format.format(Date(c.timeInMillis))
    }

    fun getCurrentDate(): String {
        var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")
        return format.format(Date(System.currentTimeMillis()))
    }

}