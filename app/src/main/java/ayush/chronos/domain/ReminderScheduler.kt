package ayush.chronos.domain

import android.content.Context

interface ReminderScheduler {
    fun schedule(reminder: Reminder, context: Context)
    fun cancel(reminder: Reminder, context: Context)
}
