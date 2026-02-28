// commonMain/kotlin/org/angelus/magitek/model/EditModeController.kt

package org.angelus.magitek.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*

/**
 * Contrôle le mode édition.
 * Déverrouillé par une séquence secrète de boutons.
 * Se reverrouille automatiquement après inactivité.
 */
class EditModeController(
    private val unlockSequence : List<Int>,     // séquence secrète
    private val lockSequence   : List<Int> = emptyList(), // séquence pour reverrouiller manuellement
    private val timeoutMs      : Long = 60_000L, // timeout inactivité (1 min par défaut)
) {
    var isUnlocked by mutableStateOf(false)
        private set

    private var progress     = 0
    private var lockProgress = 0
    private var timeoutJob: Job? = null

    /**
     * Appelé à chaque pression de bouton.
     * Retourne true si le mode vient de changer.
     */
    fun onButtonPressed(buttonIndex: Int, scope: CoroutineScope): Boolean {
        if (isUnlocked) {
            // Réinitialise le timeout à chaque interaction
            resetTimeout(scope)

            // Tente la séquence de verrouillage manuel
            if (lockSequence.isNotEmpty()) {
                if (lockSequence.getOrNull(lockProgress) == buttonIndex) {
                    lockProgress++
                    if (lockProgress >= lockSequence.size) {
                        lock()
                        return true
                    }
                } else {
                    lockProgress = 0
                }
            }
            return false
        }

        // Mode verrouillé — tente la séquence de déverrouillage
        if (unlockSequence.getOrNull(progress) == buttonIndex) {
            progress++
            if (progress >= unlockSequence.size) {
                unlock(scope)
                return true
            }
        } else {
            // Mauvais bouton — repart du début si ce bouton commence la séquence
            progress = if (unlockSequence.firstOrNull() == buttonIndex) 1 else 0
        }
        return false
    }

    private fun unlock(scope: CoroutineScope) {
        isUnlocked   = true
        progress      = 0
        lockProgress  = 0
        resetTimeout(scope)
    }

    private fun lock() {
        isUnlocked   = false
        progress      = 0
        lockProgress  = 0
        timeoutJob?.cancel()
        timeoutJob    = null
    }

    private fun resetTimeout(scope: CoroutineScope) {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(timeoutMs)
            lock()
        }
    }

    fun forceUnlock(scope: CoroutineScope) = unlock(scope)
    fun forceLock()                         = lock()
}

// Séquence secrète hardcodée — à changer avant distribution !
// Exemple : boutons 0, 7, 3, 15, 8  (indices dans la grille)
fun buildEditModeController() = EditModeController(
    unlockSequence = listOf(0, 7, 3, 15, 8),
    lockSequence   = listOf(0, 7),            // séquence courte pour reverrouiller
    timeoutMs      = 120_000L,                // 2 minutes
)