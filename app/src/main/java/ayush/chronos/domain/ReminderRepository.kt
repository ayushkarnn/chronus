package ayush.chronos.domain

import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(userId: String): Flow<List<Reminder>>
    suspend fun addReminder(reminder: Reminder)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(userId: String, reminderId: String)
}
