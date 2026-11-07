package se.ak4.ova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import se.ak4.ova.data.QuestionRepository
import se.ak4.ova.data.StudyViewModel
import se.ak4.ova.ui.Ak4App
import se.ak4.ova.ui.theme.Ak4Theme

class MainActivity : ComponentActivity() {
    private lateinit var studyVm: StudyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        studyVm = ViewModelProvider(this)[StudyViewModel::class.java]
        val repo = QuestionRepository(this)
        setContent {
            Ak4Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Ak4App(repo, studyVm.store)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::studyVm.isInitialized) studyVm.store.onForeground()
    }

    override fun onStop() {
        if (::studyVm.isInitialized) studyVm.store.onBackground()
        super.onStop()
    }
}
