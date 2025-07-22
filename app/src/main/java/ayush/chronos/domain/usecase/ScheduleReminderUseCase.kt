package ayush.chronos.domain.usecase

import android.content.Context
import ayush.chronos.domain.Reminder
import ayush.chronos.domain.ReminderScheduler

class ScheduleReminderUseCase(val scheduler: ReminderScheduler) {
    fun invoke(reminder: Reminder, context: Context) = scheduler.schedule(reminder, context)
}
