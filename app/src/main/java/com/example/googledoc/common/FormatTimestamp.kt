package com.example.googledoc.common

import java.text.SimpleDateFormat
import java.util.*

fun FormatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}