package org.angelus.magitek

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.angelus.magitek.settings.appContext
import org.angelus.magitek.settings.currentActivityRef
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
      //  enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appContext = applicationContext   // ← AVANT setContent
        currentActivityRef = WeakReference(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}