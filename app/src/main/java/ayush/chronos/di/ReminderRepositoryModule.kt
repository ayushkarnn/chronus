package ayush.chronos.di

import ayush.chronos.domain.ReminderRepository
import ayush.chronos.data.repository.ReminderRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository

    companion object {
        @Provides
        @Singleton
        fun provideReminderRepositoryImpl(firestore: FirebaseFirestore): ReminderRepositoryImpl =
            ReminderRepositoryImpl(firestore)
    }
}
