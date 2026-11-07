package se.ak4.ova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.ak4.ova.QuizViewModel
import se.ak4.ova.data.Question
import se.ak4.ova.data.QuestionRepository
import se.ak4.ova.data.StudyStore
import se.ak4.ova.data.SubjectStat
import se.ak4.ova.data.WeekSummary
import se.ak4.ova.data.formatDuration

private val DayLabels = listOf("må", "ti", "on", "to", "fr", "lö", "sö")

@Composable
fun Ak4App(repo: QuestionRepository, store: StudyStore) {
    val nav = rememberNavController()
    val quiz: QuizViewModel = viewModel()

    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(repo, store, nav, quiz) }
        composable("subject/{subject}") { entry ->
            SubjectScreen(
                subject = entry.arguments?.getString("subject").orEmpty(),
                repo = repo,
                nav = nav,
                quiz = quiz
            )
        }
        composable("stats") { StatsScreen(store, nav) }
        composable("quiz") { QuizScreen(nav, quiz, store) }
        composable("result") { ResultScreen(nav, quiz) }
        composable("review") { ReviewScreen(nav, quiz) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    repo: QuestionRepository,
    store: StudyStore,
    nav: NavHostController,
    quiz: QuizViewModel
) {
    val week = store.thisWeek
    val math = week.bySubject["Matematik"] ?: SubjectStat()
    Scaffold(topBar = { TopAppBar(title = { Text("Åk 4 Öva") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Matematik, svenska och NO för årskurs 4. Varje fråga har facit.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${repo.questions.size} frågor · mest matematik",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { nav.navigate("stats") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Den här veckan", fontWeight = FontWeight.Bold)
                    Text(week.label, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Öppnad ${week.opens} ${if (week.opens == 1) "gång" else "gånger"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Tid i appen: ${formatDuration(week.foregroundMs)}")
                    Text("Övningstid: ${formatDuration(week.practiceMs)}")
                    Text("Matte: ${math.answered} frågor, ${formatDuration(math.ms)}")
                    Spacer(Modifier.height(4.dp))
                    Text("Tryck för alla veckor", style = MaterialTheme.typography.bodySmall)
                }
            }
            HomeCard(
                title = "Starta matte",
                subtitle = "20 frågor · ${repo.count("Matematik")} i banken",
                icon = Icons.Default.Calculate,
                container = MaterialTheme.colorScheme.primaryContainer
            ) {
                quiz.start(repo.bySubject("Matematik").shuffled().take(20))
                nav.navigate("quiz")
            }
            HomeCard(
                title = "Mer matematik",
                subtitle = "10, 30 eller alla mattefrågor",
                icon = Icons.Default.PlayArrow,
                container = MaterialTheme.colorScheme.primaryContainer
            ) { nav.navigate("subject/Matematik") }
            HomeCard(
                title = "Svenska",
                subtitle = "${repo.count("Svenska")} frågor",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                container = Color(0xFFFFF3E0)
            ) { nav.navigate("subject/Svenska") }
            HomeCard(
                title = "NO",
                subtitle = "${repo.count("NO")} frågor om naturen",
                icon = Icons.Default.Park,
                container = Color(0xFFE0F2F1)
            ) { nav.navigate("subject/NO") }
            HomeCard(
                title = "Statistik",
                subtitle = "Tid och antal gånger varje vecka",
                icon = Icons.Default.BarChart,
                container = MaterialTheme.colorScheme.surfaceVariant
            ) { nav.navigate("stats") }
        }
    }
}

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    container: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    subject: String,
    repo: QuestionRepository,
    nav: NavHostController,
    quiz: QuizViewModel
) {
    val all = repo.bySubject(subject)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("${all.size} frågor med facit.", style = MaterialTheme.typography.bodyLarge)
            listOf(10 to "Kort pass", 20 to "Vanligt pass", 30 to "Långt pass").forEach { (n, label) ->
                if (n <= all.size || n == 10) {
                    Button(
                        onClick = {
                            quiz.start(all.shuffled().take(n.coerceAtMost(all.size)))
                            nav.navigate("quiz")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("$label · $n frågor") }
                }
            }
            FilledTonalButton(
                onClick = {
                    quiz.start(all.shuffled())
                    nav.navigate("quiz")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Alla ${all.size} frågor") }
            Spacer(Modifier.height(8.dp))
            Text("Öva ett område", fontWeight = FontWeight.SemiBold)
            repo.categories(subject).forEach { cat ->
                val n = repo.byCategory(subject, cat).size
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            quiz.start(repo.byCategory(subject, cat).shuffled())
                            nav.navigate("quiz")
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(cat, style = MaterialTheme.typography.titleMedium)
                        Text("$n frågor")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(store: StudyStore, nav: NavHostController) {
    val weeks = store.weeks
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Här syns hur många gånger appen öppnats och hur länge han övar, vecka för vecka. Allt sparas på den här telefonen.",
                style = MaterialTheme.typography.bodyMedium
            )
            weeks.forEachIndexed { index, week ->
                WeekCard(week, highlight = index == 0)
            }
        }
    }
}

@Composable
private fun WeekCard(week: WeekSummary, highlight: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (highlight) "Den här veckan" else "Tidigare vecka",
                fontWeight = FontWeight.Bold
            )
            Text(week.label)
            Text(
                "Öppnad ${week.opens} ${if (week.opens == 1) "gång" else "gånger"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Tid i appen: ${formatDuration(week.foregroundMs)}")
            Text("Övningstid: ${formatDuration(week.practiceMs)}")
            Text("Frågor: ${week.answered} · rätt: ${week.correct}")
            DayBars(week.dayMinutes)
            listOf("Matematik", "Svenska", "NO").forEach { name ->
                val stat = week.bySubject[name] ?: SubjectStat()
                val percent = if (stat.answered == 0) 0 else (stat.correct * 100) / stat.answered
                Text("$name: ${formatDuration(stat.ms)}, ${stat.answered} frågor, $percent % rätt, ${stat.quizzes} pass")
            }
        }
    }
}

@Composable
private fun DayBars(minutes: List<Int>) {
    val max = minutes.maxOrNull()?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        minutes.forEachIndexed { i, min ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val bar = ((72f * min) / max).coerceAtLeast(if (min > 0) 8f else 2f)
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(bar.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (min > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(DayLabels.getOrElse(i) { "" }, style = MaterialTheme.typography.labelSmall)
                Text(if (min > 0) "$min" else "·", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizScreen(nav: NavHostController, quiz: QuizViewModel, store: StudyStore) {
    DisposableEffect(Unit) {
        store.bind(quiz)
        quiz.resumeClock()
        onDispose {
            quiz.pauseClock(store)
            store.bind(null)
        }
    }
    LaunchedEffect(quiz.finished, quiz.current) {
        if (quiz.finished || quiz.current == null) {
            nav.navigate("result") { popUpTo("quiz") { inclusive = true } }
        }
    }
    val q = quiz.current ?: return
    val total = quiz.questions.size
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fråga ${quiz.index + 1} av $total") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            LinearProgressIndicator(
                progress = { (quiz.index + 1f) / total.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(q.subject, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(q.category, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(10.dp))
            Text(q.question, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(18.dp))
            q.options.forEachIndexed { i, option ->
                OptionRow(
                    letter = ('A' + i).toString(),
                    text = option,
                    selected = quiz.selected == i,
                    revealed = quiz.revealed,
                    isCorrect = i == q.correctIndex,
                    onClick = { quiz.choose(i) }
                )
                Spacer(Modifier.height(10.dp))
            }
            if (quiz.revealed) {
                FacitBox(q, quiz.selected == q.correctIndex)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { quiz.next() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (quiz.index >= quiz.questions.lastIndex) "Visa resultat" else "Nästa fråga")
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    letter: String,
    text: String,
    selected: Boolean,
    revealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val bg = when {
        revealed && isCorrect -> scheme.tertiary.copy(alpha = 0.22f)
        revealed && selected && !isCorrect -> scheme.error.copy(alpha = 0.18f)
        selected -> scheme.primaryContainer
        else -> scheme.surfaceVariant
    }
    val border = when {
        revealed && isCorrect -> scheme.tertiary
        revealed && selected && !isCorrect -> scheme.error
        else -> scheme.outline.copy(alpha = 0.35f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(scheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(letter, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (revealed && isCorrect) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Rätt", tint = scheme.tertiary)
        } else if (revealed && selected && !isCorrect) {
            Icon(Icons.Outlined.Cancel, contentDescription = "Fel", tint = scheme.error)
        }
    }
}

@Composable
private fun FacitBox(q: Question, correct: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (correct) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (correct) "Rätt!" else "Inte riktigt. Rätt svar är ${('A' + q.correctIndex)}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text("Facit", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(q.facit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(nav: NavHostController, quiz: QuizViewModel) {
    val total = quiz.questions.size.coerceAtLeast(1)
    val percent = remember(quiz.score, total) { (quiz.score * 100) / total }
    val message = when {
        percent >= 90 -> "Jättebra!"
        percent >= 70 -> "Bra jobbat!"
        else -> "Fortsätt öva, du lär dig!"
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Resultat") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("${quiz.score} / ${quiz.questions.size}", style = MaterialTheme.typography.displaySmall)
            Text("$percent % rätt", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { nav.navigate("review") }, modifier = Modifier.fillMaxWidth()) {
                Text("Visa facit")
            }
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    quiz.start(quiz.questions.shuffled())
                    nav.navigate("quiz") { popUpTo("home") }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Replay, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Gör om")
            }
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = { nav.popBackStack("home", inclusive = false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Till startsidan") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewScreen(nav: NavHostController, quiz: QuizViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facit") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            quiz.history.forEachIndexed { i, item ->
                Card {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Fråga ${i + 1} · ${if (item.isCorrect) "Rätt" else "Fel"}",
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isCorrect) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(item.question.question)
                        Spacer(Modifier.height(6.dp))
                        Text("Ditt svar: ${item.question.options.getOrNull(item.chosenIndex) ?: "-"}")
                        Text("Rätt svar: ${item.question.options[item.question.correctIndex]}")
                        Spacer(Modifier.height(6.dp))
                        Text(item.question.facit, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
