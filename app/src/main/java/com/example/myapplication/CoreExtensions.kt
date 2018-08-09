package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.*

fun Date.toISOtFormat():String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+0000'", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormat.format(this)
}