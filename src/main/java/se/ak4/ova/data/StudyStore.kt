package se.ak4.ova.data

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import se.ak4.ova.QuizViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SubjectStat(
    val ms: Long = 0,
    val answered: Int = 0,
    val correct: Int = 0,
    val quizzes: Int = 0
)

data class DayRecord(
    val dayStart: Long,
    val opens: Int = 0,
    val foregroundMs: Long = 0,
    val subjects: Map<String, SubjectStat> = emptyMap()
)

data class WeekSummary(
    val monday: Long,
    val label: String,
    val opens: Int,
    val foregroundMs: Long,
    val practiceMs: Long,
    val answered: Int,
    val correct: Int,
    val bySubject: Map<String, SubjectStat>,
    val dayMinutes: List<Int>
)

class StudyStore(context: Context) {
    private val file = File(context.filesDir, "study-log.json")
    private var quiz: QuizViewModel? = null
    private var fgStart = 0L
    private var lastStopWall = 0L

    var days by mutableStateOf(load())
        private set

    fun bind(viewModel: QuizViewModel?) {
        quiz = viewModel
    }

    fun onForeground() {
        quiz?.resumeClock()
        val now = System.currentTimeMillis()
        if (fgStart == 0L) {
            if (now - lastStopWall > 120_000L) addOpen()
            fgStart = SystemClock.elapsedRealtime()
        }
    }

    fun onBackground() {
        quiz?.pauseClock(this)
        if (fgStart != 0L) {
            val dt = (SystemClock.elapsedRealtime() - fgStart).coerceAtLeast(0)
            fgStart = 0L
            lastStopWall = System.currentTimeMillis()
            addForeground(dt)
        }
    }

    fun addPractice(subject: String, ms: Long, answered: Int, correct: Int, quizzes: Int) {
        val current = today()
        val prev = current.subjects[subject] ?: SubjectStat()
        val next = prev.copy(
            ms = prev.ms + ms.coerceAtLeast(0),
            answered = prev.answered + answered,
            correct = prev.correct + correct,
            quizzes = prev.quizzes + quizzes
        )
        replace(current.copy(subjects = current.subjects + (subject to next)))
    }

    val thisWeek: WeekSummary
        get() = summarize(mondayOf(startOfDay(System.currentTimeMillis())))

    val weeks: List<WeekSummary>
        get() {
            val current = mondayOf(startOfDay(System.currentTimeMillis()))
            if (days.isEmpty()) return listOf(summarize(current))
            val first = mondayOf(days.minOf { it.dayStart })
            val list = mutableListOf<WeekSummary>()
            var cursor = current
            var guard = 0
            while (cursor >= first && guard < 16) {
                list += summarize(cursor)
                cursor = addDays(cursor, -7)
                guard++
            }
            return list
        }

    private fun addOpen() {
        val current = today()
        replace(current.copy(opens = current.opens + 1))
    }

    private fun addForeground(ms: Long) {
        if (ms <= 0L) return
        val current = today()
        replace(current.copy(foregroundMs = current.foregroundMs + ms))
    }

    private fun today(): DayRecord {
        val start = startOfDay(System.currentTimeMillis())
        return days.find { it.dayStart == start } ?: DayRecord(start)
    }

    private fun replace(day: DayRecord) {
        days = (days.filter { it.dayStart != day.dayStart } + day).sortedBy { it.dayStart }
        save()
    }

    private fun summarize(monday: Long): WeekSummary {
        val weekDays = (0..6).map { offset ->
            val start = addDays(monday, offset)
            days.find { it.dayStart == start }
        }
        val merged = mutableMapOf<String, SubjectStat>()
        var opens = 0
        var foreground = 0L
        weekDays.forEach { day ->
            if (day == null) return@forEach
            opens += day.opens
            foreground += day.foregroundMs
            day.subjects.forEach { (name, stat) ->
                val prev = merged[name] ?: SubjectStat()
                merged[name] = prev.copy(
                    ms = prev.ms + stat.ms,
                    answered = prev.answered + stat.answered,
                    correct = prev.correct + stat.correct,
                    quizzes = prev.quizzes + stat.quizzes
                )
            }
        }
        val practice = merged.values.sumOf { it.ms }
        val answered = merged.values.sumOf { it.answered }
        val correct = merged.values.sumOf { it.correct }
        val minutes = weekDays.map { day ->
            val ms = day?.subjects?.values?.sumOf { it.ms } ?: 0L
            (ms / 60000L).toInt()
        }
        return WeekSummary(
            monday = monday,
            label = weekLabel(monday),
            opens = opens,
            foregroundMs = foreground,
            practiceMs = practice,
            answered = answered,
            correct = correct,
            bySubject = merged,
            dayMinutes = minutes
        )
    }

    private fun load(): List<DayRecord> {
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val subjectsJson = obj.optJSONObject("subjects") ?: JSONObject()
                    val subjects = mutableMapOf<String, SubjectStat>()
                    val keys = subjectsJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val s = subjectsJson.getJSONObject(key)
                        subjects[key] = SubjectStat(
                            ms = s.optLong("ms"),
                            answered = s.optInt("answered"),
                            correct = s.optInt("correct"),
                            quizzes = s.optInt("quizzes")
                        )
                    }
                    add(
                        DayRecord(
                            dayStart = obj.getLong("dayStart"),
                            opens = obj.optInt("opens"),
                            foregroundMs = obj.optLong("foregroundMs"),
                            subjects = subjects
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun save() {
        try {
            val array = JSONArray()
            days.forEach { day ->
                val subjectsJson = JSONObject()
                day.subjects.forEach { (name, stat) ->
                    subjectsJson.put(
                        name,
                        JSONObject()
                            .put("ms", stat.ms)
                            .put("answered", stat.answered)
                            .put("correct", stat.correct)
                            .put("quizzes", stat.quizzes)
                    )
                }
                array.put(
                    JSONObject()
                        .put("dayStart", day.dayStart)
                        .put("opens", day.opens)
                        .put("foregroundMs", day.foregroundMs)
                        .put("subjects", subjectsJson)
                )
            }
            file.writeText(array.toString(2))
        } catch (_: Exception) {
            // Statistiken får inte krascha övningen.
        }
    }
}

class StudyViewModel(app: Application) : AndroidViewModel(app) {
    val store = StudyStore(app)
}

fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0 min"
    val totalMin = ms / 60000L
    if (totalMin < 1L) return "mindre än 1 min"
    val hours = totalMin / 60L
    val minutes = totalMin % 60L
    return when {
        hours == 0L -> "$minutes min"
        minutes == 0L -> "$hours tim"
        else -> "$hours tim $minutes min"
    }
}

private fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun mondayOf(dayStart: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dayStart
    val daysFromMonday = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    cal.add(Calendar.DATE, -daysFromMonday)
    return startOfDay(cal.timeInMillis)
}

private fun addDays(dayStart: Long, days: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dayStart
    cal.add(Calendar.DATE, days)
    return startOfDay(cal.timeInMillis)
}

private fun weekLabel(monday: Long): String {
    val fmt = SimpleDateFormat("d MMM", Locale("sv", "SE"))
    val cal = Calendar.getInstance()
    cal.timeInMillis = monday
    val start = fmt.format(cal.time)
    cal.add(Calendar.DATE, 6)
    val end = fmt.format(cal.time)
    return "$start – $end"
}
