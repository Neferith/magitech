// commonMain/kotlin/org/angelus/magitek/model/EditModeController.kt

package org.angelus.magitek.model

import kotlinx.coroutines.*

class EditModeController(
    private val unlockSequence : List<Int>,
    private val lockSequence   : List<Int> = emptyList(),
    private val timeoutMs      : Long = 120_000L,
    private val onUnlock       : () -> Unit = {},
    private val onLock         : () -> Unit = {},
    private val onTimeout: () -> Unit = {},
) {
    private var progress     = 0
    private var lockProgress = 0
    private var timeoutJob: Job? = null


    /**
     * Appelé à chaque pression de bouton.
     * Retourne true si le mode vient de changer.
     */
    fun onButtonPressed(buttonIndex: Int, scope: CoroutineScope, isUnlocked: Boolean): Boolean {
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
        progress     = 0
        lockProgress = 0
        onUnlock()
        resetTimeout(scope)
    }

    private fun lock() {
        progress     = 0
        lockProgress = 0
        timeoutJob?.cancel()
        timeoutJob   = null
        onLock()
        onTimeout()   // ← appelé uniquement au timeout
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

fun buildEditModeController(
    onUnlock: () -> Unit = {},
    onLock  : () -> Unit = {},
    onTimeout: () -> Unit = {},
) = EditModeController(
    unlockSequence = listOf(0, 7, 3, 15, 8),
    lockSequence   = listOf(0, 7),
    timeoutMs      = 240_000L,
    onUnlock       = onUnlock,
    onLock         = onLock,
    onTimeout      = onTimeout,
)