package org.angelus.magitek

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import org.angelus.magitek.model.buildEditModeController
import org.angelus.magitek.platform.DeviceOrientation
import org.angelus.magitek.platform.rememberDeviceOrientation
import org.angelus.magitek.ui.BackOfRemoteScreen

@Composable
@Preview
fun App() {
    /*MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }*/
    val orientation = rememberDeviceOrientation()

    // rememberSaveable survit à la rotation
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var screenLog  by rememberSaveable {
        mutableStateOf(listOf("> ATTENTE COMMANDE...", "> _"))
    }

    val editModeController = remember {
        buildEditModeController(
            onUnlock = { isUnlocked = true },
            onLock   = { isUnlocked = false },
            onTimeout = {
                screenLog = listOf(
                    "> MODE ÉDITION DÉSACTIVÉ",
                    "> SESSION EXPIRÉE",
                )
            },
        )
    }

    GarlemaldTheme {
        when (orientation) {
            DeviceOrientation.LANDSCAPE -> BackOfRemoteScreen()
            DeviceOrientation.PORTRAIT  -> MagitekRemoteScreen(
                editModeController = editModeController,
                isEditMode         = isUnlocked,
                screenLog          = screenLog,
                onScreenLogChange  = { screenLog = it },
            )
        }
    }
}