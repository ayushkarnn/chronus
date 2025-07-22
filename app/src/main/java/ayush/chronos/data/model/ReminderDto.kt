package ayush.chronos.data.model

import ayush.chronos.domain.Reminder


data class ReminderDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val dateTime: Long = 0L,
    val notes: String? = null,
    val imageUrl: String? = null
) {
    constructor() : this("", "", "", 0L, null, null) //Needed for firestore
}

fun ReminderDto.toDomain() = Reminder(
    id = id,
    userId = userId,
    title = title,
    dateTime = dateTime,
    notes = notes,
    imageUrl = imageUrl
)

fun Reminder.toDto() = ReminderDto(
    id = id,
    userId = userId,
    title = title,
    dateTime = dateTime,
    notes = notes,
    imageUrl = imageUrl
)
