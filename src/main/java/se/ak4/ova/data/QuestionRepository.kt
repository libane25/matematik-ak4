package se.ak4.ova.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuestionRepository(context: Context) {
    val questions: List<Question> = run {
        val json = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Question>>() {}.type
        Gson().fromJson<List<Question>>(json, type)
    }

    fun subjects(): List<String> = questions.map { it.subject }.distinct()

    fun count(subject: String): Int = questions.count { it.subject == subject }

    fun categories(subject: String): List<String> =
        questions.filter { it.subject == subject }.map { it.category }.distinct()

    fun bySubject(subject: String): List<Question> =
        questions.filter { it.subject == subject }

    fun byCategory(subject: String, category: String): List<Question> =
        questions.filter { it.subject == subject && it.category == category }
}
