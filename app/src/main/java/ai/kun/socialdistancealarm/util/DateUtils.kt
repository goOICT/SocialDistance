package ai.kun.socialdistancealarm.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getCurrentFormattedDateString(format: String, timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat(format, Locale.US)
        return dateFormat.format(timeInMillis)
    }
}