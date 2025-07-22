package com.app.chronus.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val REMINDER_INFO_WITH_ID = stringPreferencesKey("reminder_info_with_id")
        private val SCHEDULED_REMINDER_IDS = stringSetPreferencesKey("scheduled_reminder_ids")
    }

    suspend fun getReminderInfoWithId() = dataStore.data.map { it[REMINDER_INFO_WITH_ID] }.first()
    suspend fun setReminderInfoWithId(id: String) {
        dataStore.edit { it[REMINDER_INFO_WITH_ID] = id }
    }

    suspend fun getScheduledReminderIds(): Set<String> =
        dataStore.data.map { it[SCHEDULED_REMINDER_IDS] ?: emptySet() }.first()

    suspend fun addScheduledReminderId(id: String) {
        dataStore.edit {
            val current = it[SCHEDULED_REMINDER_IDS] ?: emptySet()
            it[SCHEDULED_REMINDER_IDS] = current + id
        }
    }

    suspend fun removeScheduledReminderId(id: String) {
        dataStore.edit {
            val current = it[SCHEDULED_REMINDER_IDS] ?: emptySet()
            it[SCHEDULED_REMINDER_IDS] = current - id
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { it.clear() }
    }

}