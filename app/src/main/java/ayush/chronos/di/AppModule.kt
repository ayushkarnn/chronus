package ayush.chronos.di

import ayush.chronos.data.notification.ReminderSchedulerImpl
import ayush.chronos.domain.ReminderScheduler
import ayush.chronos.domain.usecase.ScheduleReminderUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideReminderScheduler(): ReminderScheduler = ReminderSchedulerImpl()

    @Provides
    @Singleton
    fun provideScheduleReminderUseCase(
        scheduler: ReminderScheduler
    ): ScheduleReminderUseCase = ScheduleReminderUseCase(scheduler)
}
