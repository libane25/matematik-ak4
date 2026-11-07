package se.ak4.ova

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import se.ak4.ova.data.Question
import se.ak4.ova.data.StudyStore

data class AnsweredQuestion(
    val question: Question,
    val chosenIndex: Int
) {
    val isCorrect: Boolean get() = chosenIndex == question.correctIndex
}

class QuizViewModel : ViewModel() {
    var questions: List<Question> by mutableStateOf(emptyList())
        private set
    var index by mutableIntStateOf(0)
        private set
    var selected by mutableStateOf<Int?>(null)
        private set
    var revealed by mutableStateOf(false)
        private set
    var score by mutableIntStateOf(0)
        private set
    var finished by mutableStateOf(false)
        private set
    var history by mutableStateOf(listOf<AnsweredQuestion>())
        private set

    private var lastTick = 0L
    private var clockRunning = false
    private var quizCountSent = false
    private var pendingAnswered = 0
    private var pendingCorrect = 0

    val current: Question? get() = questions.getOrNull(index)
    val subject: String get() = questions.firstOrNull()?.subject.orEmpty()

    fun start(qs: List<Question>) {
        questions = qs
        index = 0
        selected = null
        revealed = false
        score = 0
        finished = false
        history = emptyList()
        lastTick = 0L
        clockRunning = false
        quizCountSent = false
        pendingAnswered = 0
        pendingCorrect = 0
    }

    fun choose(optionIndex: Int) {
        if (revealed || finished) return
        selected = optionIndex
        revealed = true
        val q = current ?: return
        if (optionIndex == q.correctIndex) score++
        history = history + AnsweredQuestion(q, optionIndex)
    }

    fun next() {
        if (index >= questions.lastIndex) {
            finished = true
        } else {
            index++
            selected = null
            revealed = false
        }
    }

    fun resumeClock() {
        if (questions.isEmpty() || finished) return
        lastTick = SystemClock.elapsedRealtime()
        clockRunning = true
    }

    fun pauseClock(store: StudyStore) {
        if (subject.isBlank()) return
        var dt = 0L
        if (clockRunning) {
            val now = SystemClock.elapsedRealtime()
            dt = (now - lastTick).coerceAtLeast(0)
            clockRunning = false
            lastTick = now
        }
        val answeredNow = history.size
        val dA = (answeredNow - pendingAnswered).coerceAtLeast(0)
        val dC = (score - pendingCorrect).coerceAtLeast(0)
        pendingAnswered = answeredNow
        pendingCorrect = score
        val countQuiz = if (!quizCountSent && answeredNow > 0) {
            quizCountSent = true
            1
        } else {
            0
        }
        if (dt == 0L && dA == 0 && countQuiz == 0) return
        store.addPractice(subject, dt, dA, dC, countQuiz)
    }
}
