// commonMain/kotlin/org/angelus/magitek/model/TranslatorController.kt

package org.angelus.magitek.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*

class TranslatorController(
    private val sequence: List<Int>,
) {
    var isActive by mutableStateOf(false)
        private set

    // Buffer de codes saisis (ex: ["XAR", "XOR", "XIK"])
    var inputCodes by mutableStateOf<List<String>>(emptyList())
        private set

    // Texte décodé en temps réel
    val decodedText: String get() =
        MagitekCipher.decode(inputCodes).ifEmpty { "—" }

    private var progress = 0

    /**
     * Appelé à chaque pression de bouton.
     * Retourne true si le mode vient de changer.
     */
    fun onButtonPressed(buttonIndex: Int): Boolean {
        // Détection de la séquence
        if (sequence.getOrNull(progress) == buttonIndex) {
            progress++
            if (progress >= sequence.size) {
                progress  = 0
                isActive  = !isActive
                if (!isActive) inputCodes = emptyList()  // reset à la désactivation
                return true
            }
        } else {
            progress = if (sequence.firstOrNull() == buttonIndex) 1 else 0
        }

        // En mode actif — ajouter le code du bouton
        if (isActive) {
            inputCodes = inputCodes + ButtonLabelEncoder.encode(buttonIndex)
        }

        return false
    }

    fun deleteLast() {
        if (inputCodes.isNotEmpty())
            inputCodes = inputCodes.dropLast(1)
    }

    fun clear() {
        inputCodes = emptyList()
    }
}

fun buildTranslatorController() = TranslatorController(
    sequence = listOf(0, 7, 63, 56),   // RIK NOL NIL NOL RIK — à changer avant distribution
)