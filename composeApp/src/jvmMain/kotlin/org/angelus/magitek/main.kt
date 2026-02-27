package org.angelus.magitek

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "makitek_remote",
    ) {
        App()
    }
}