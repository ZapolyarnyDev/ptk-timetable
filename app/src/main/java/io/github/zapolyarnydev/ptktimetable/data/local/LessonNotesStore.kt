package io.github.zapolyarnydev.ptktimetable.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val LESSON_NOTES_DATASTORE = "lesson_notes"
private val Context.lessonNotesDataStore by preferencesDataStore(name = LESSON_NOTES_DATASTORE)

data class LessonNote(
    val id: String,
    val groupName: String,
    val date: LocalDate,
    val timeRange: String,
    val weekType: String,
    val subject: String,
    val teacher: String?,
    val classroom: String?,
    val rawText: String,
    val noteText: String,
    val reminderEnabled: Boolean,
    val reminderMinutes: Int?,
    val remindAtEpochMillis: Long?,
    val createdAtEpochMillis: Long
)

class LessonNotesStore(
    private val context: Context
) {

    suspend fun getAll(): List<LessonNote> {
        val raw = context.lessonNotesDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()[NOTES_JSON_KEY]
            .orEmpty()

        return parse(raw)
    }

    suspend fun upsert(note: LessonNote) {
        val current = getAll().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == note.id }
        if (existingIndex >= 0) {
            current[existingIndex] = note
        } else {
            current.add(note)
        }
        save(current)
    }

    suspend fun remove(noteId: String) {
        val current = getAll().filterNot { it.id == noteId }
        save(current)
    }

    private suspend fun save(notes: List<LessonNote>) {
        val json = JSONArray().apply {
            notes.forEach { note ->
                put(
                    JSONObject().apply {
                        put("id", note.id)
                        put("groupName", note.groupName)
                        put("date", note.date.format(DATE_FORMATTER))
                        put("timeRange", note.timeRange)
                        put("weekType", note.weekType)
                        put("subject", note.subject)
                        put("teacher", note.teacher ?: JSONObject.NULL)
                        put("classroom", note.classroom ?: JSONObject.NULL)
                        put("rawText", note.rawText)
                        put("noteText", note.noteText)
                        put("reminderEnabled", note.reminderEnabled)
                        put("reminderMinutes", note.reminderMinutes ?: JSONObject.NULL)
                        put("remindAtEpochMillis", note.remindAtEpochMillis ?: JSONObject.NULL)
                        put("createdAtEpochMillis", note.createdAtEpochMillis)
                    }
                )
            }
        }.toString()

        context.lessonNotesDataStore.edit { prefs ->
            prefs[NOTES_JSON_KEY] = json
        }
    }

    private fun parse(raw: String): List<LessonNote> {
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val id = obj.optString("id").trim()
                    val groupName = obj.optString("groupName").trim()
                    val dateRaw = obj.optString("date").trim()
                    val timeRange = obj.optString("timeRange").trim()
                    val weekType = obj.optString("weekType").trim()
                    val rawText = obj.optString("rawText").trim()
                    val noteText = obj.optString("noteText").trim()
                    if (id.isBlank() || groupName.isBlank() || dateRaw.isBlank()) continue

                    val date = runCatching { LocalDate.parse(dateRaw, DATE_FORMATTER) }.getOrNull() ?: continue
                    val reminderEnabled = obj.optBoolean("reminderEnabled", false)
                    val reminderMinutes = if (obj.isNull("reminderMinutes")) {
                        null
                    } else {
                        obj.optInt("reminderMinutes")
                    }.takeIf { it != null && it > 0 }
                    val remindAtEpochMillis = if (obj.isNull("remindAtEpochMillis")) {
                        null
                    } else {
                        obj.optLong("remindAtEpochMillis")
                    }
                    val createdAt = obj.optLong("createdAtEpochMillis").takeIf { it > 0L }
                        ?: Instant.now().toEpochMilli()

                    add(
                        LessonNote(
                            id = id,
                            groupName = groupName,
                            date = date,
                            timeRange = timeRange,
                            weekType = weekType,
                            subject = obj.optString("subject"),
                            teacher = obj.optString("teacher").takeIf { it.isNotBlank() },
                            classroom = obj.optString("classroom").takeIf { it.isNotBlank() },
                            rawText = rawText,
                            noteText = noteText,
                            reminderEnabled = reminderEnabled,
                            reminderMinutes = reminderMinutes,
                            remindAtEpochMillis = remindAtEpochMillis,
                            createdAtEpochMillis = createdAt
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private val NOTES_JSON_KEY: Preferences.Key<String> = stringPreferencesKey("lesson_notes_json")
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun buildLessonNoteId(
            groupName: String,
            date: LocalDate,
            timeRange: String,
            weekType: String,
            rawText: String
        ): String {
            return listOf(
                groupName.trim(),
                date.format(DATE_FORMATTER),
                timeRange.trim(),
                weekType.trim(),
                rawText.trim().hashCode().toString()
            ).joinToString("|")
        }

        fun parseStartTimeOrNull(timeRange: String): LocalTime? {
            val normalized = timeRange
                .replace('—', '-')
                .replace('–', '-')
                .replace(" ", "")
            val start = normalized.split("-", limit = 2).firstOrNull().orEmpty()
            val match = Regex("(\\d{1,2})[.:](\\d{2})").find(start) ?: return null
            val h = match.groupValues[1].toIntOrNull() ?: return null
            val m = match.groupValues[2].toIntOrNull() ?: return null
            return runCatching { LocalTime.of(h, m) }.getOrNull()
        }
    }
}
