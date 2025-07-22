package ayush.chronos.data.repository

import ayush.chronos.domain.Reminder
import ayush.chronos.domain.ReminderRepository
import ayush.chronos.data.model.ReminderDto
import ayush.chronos.data.model.toDomain
import ayush.chronos.data.model.toDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReminderRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ReminderRepository {
    private fun remindersCollection(userId: String) =
        firestore.collection("users").document(userId).collection("reminders")

    override fun getReminders(userId: String): Flow<List<Reminder>> = callbackFlow {
        val listener = remindersCollection(userId)
            .addSnapshotListener { value, _ ->
                val list = value?.documents?.mapNotNull {
                    it.toObject(ReminderDto::class.java)?.copy(id = it.id)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addReminder(reminder: Reminder) {
        val ref = remindersCollection(reminder.userId).document()
        ref.set(reminder.copy(id = ref.id).toDto()).await()
    }

    override suspend fun updateReminder(reminder: Reminder) {
        if (reminder.id.isBlank()) return
        remindersCollection(reminder.userId).document(reminder.id).set(reminder.toDto()).await()
    }

    override suspend fun deleteReminder(userId: String, reminderId: String) {
        if (userId.isBlank() || reminderId.isBlank()) return
        remindersCollection(userId).document(reminderId).delete().await()
    }
}
