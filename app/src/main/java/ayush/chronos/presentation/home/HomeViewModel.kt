package ayush.chronos.presentation.home

import ayush.chronos.domain.Reminder
import ayush.chronos.domain.ReminderRepository
import ayush.chronos.domain.usecase.ScheduleReminderUseCase
import com.app.chronus.prefs.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import ayush.chronos.data.repository.PollinationRepo

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val auth: FirebaseAuth,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val appPreferences: AppPreferences,
    private val pollinationRepo: PollinationRepo,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val userId: String get() = auth.currentUser?.uid.orEmpty()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _scheduledReminderIds = MutableStateFlow<Set<String>>(emptySet())
    val scheduledReminderIds: StateFlow<Set<String>> = _scheduledReminderIds.asStateFlow()

    // AI Wish states
    private val _aiResponseState = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponseState: StateFlow<AiResponseState> = _aiResponseState.asStateFlow()

    init {
        loadReminders()
        loadScheduledReminderIds()
    }

    private fun loadReminders() {
        if (userId.isBlank()) return
        repository.getReminders(userId).onEach { _reminders.value = it }
            .launchIn(viewModelScope)
    }

    fun loadScheduledReminderIds() {
        viewModelScope.launch {
            _scheduledReminderIds.value = appPreferences.getScheduledReminderIds()
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val userReminder = reminder.copy(userId = userId)
            repository.addReminder(userReminder)
            if (userReminder.dateTime > System.currentTimeMillis()) {
                scheduleReminderUseCase.invoke(userReminder, context)
                appPreferences.addScheduledReminderId(userReminder.id)
            } else {
                scheduleReminderUseCase.scheduler.cancel(userReminder, context)
                appPreferences.removeScheduledReminderId(userReminder.id)
            }
            loadReminders()
            loadScheduledReminderIds()
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val userReminder = reminder.copy(userId = userId)
            repository.updateReminder(userReminder)
            if (userReminder.dateTime > System.currentTimeMillis()) {
                scheduleReminderUseCase.invoke(userReminder, context)
                appPreferences.addScheduledReminderId(userReminder.id)
            } else {
                scheduleReminderUseCase.scheduler.cancel(userReminder, context)
                appPreferences.removeScheduledReminderId(userReminder.id)
            }
            loadReminders()
            loadScheduledReminderIds()
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(userId, reminderId)
            val canceledReminder = Reminder(reminderId, userId, "", 0L, null, null)
            scheduleReminderUseCase.scheduler.cancel(canceledReminder, context)
            appPreferences.removeScheduledReminderId(reminderId)
            loadReminders()
            loadScheduledReminderIds()
        }
    }

    suspend fun getAllScheduledReminderIds(): Set<String> =
        appPreferences.getScheduledReminderIds()

    fun fetchAiResponse(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiResponseState.value = AiResponseState.Loading
            try {
                val wish = pollinationRepo.getData(prompt)
                if (wish != null) {
                    _aiResponseState.value = AiResponseState.Success(wish)
                } else {
                    _aiResponseState.value = AiResponseState.Error("No response")
                }
            } catch (e: Exception) {
                _aiResponseState.value = AiResponseState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetAiResponseState() {
        _aiResponseState.value = AiResponseState.Idle
    }
}

sealed class AiResponseState {
    object Idle : AiResponseState()
    object Loading : AiResponseState()
    data class Success(val wish: String) : AiResponseState()
    data class Error(val message: String) : AiResponseState()
}
