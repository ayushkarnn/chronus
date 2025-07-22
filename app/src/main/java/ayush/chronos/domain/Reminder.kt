package ayush.chronos.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Reminder(
    val id: String = "", // Firestore ID
    val userId: String = "",
    val title: String,
    val dateTime: Long,
    val notes: String? = null,
    val imageUrl: String? = null
)

fun Reminder.formattedDateTime(pattern: String = "EEE, d MMM yyyy h:mm a"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(dateTime))
}
