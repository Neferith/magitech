// commonMain/platform/FeedbackController.kt
package org.angelus.magitek

/**
 * Contrôleur de retour sensoriel — son + vibration au clic d'une commande magitek.
 * Implémenté via expect/actual pour chaque plateforme.
 */
import androidx.compose.runtime.Composable

expect class FeedbackController {
    fun triggerCommandFeedback()
    fun release()
}

@Composable
expect fun rememberFeedbackController(): FeedbackController
