// commonMain/kotlin/org/angelus/magitek/model/ContradictionDetector.kt

package org.angelus.magitek.model

import org.angelus.magitek.Logger

/**
 * Détecte un overflow lorsque deux commandes contradictoires
 * s'alternent toutes les 100ms pendant 20 secondes.
 *
 * Règle : si on détecte N alternances contradiction/contradiction
 * dans une fenêtre de 20 secondes → overflow.
 */
class ContradictionDetector(
    private val rules          : List<ContradictionRule>,
    private val intervalMs     : Long = 100L,
    private val durationMs     : Long = 20_000L,
    private val onOverflow     : (ContradictionRule) -> Unit,
) {
    // Historique : timestamp + commande
    private data class Entry(val timeMs: Long, val command: CommandSpec)

    private val history = ArrayDeque<Entry>()

    fun onCommandExecuted(command: CommandSpec) {
        val now = System.currentTimeMillis()
        history.addLast(Entry(now, command))

        // Purge les entrées trop anciennes
        val cutoff = now - durationMs
        while (history.isNotEmpty() && history.first().timeMs < cutoff) {
            history.removeFirst()
        }

        // Vérifie chaque règle
        for (rule in rules) {
            if (checkRule(rule, now)) {
                history.clear()
                onOverflow(rule)
                return
            }
        }
    }

    private fun checkRule(rule: ContradictionRule, now: Long): Boolean {
        // Filtre les entrées concernées par cette règle
        val relevant = history.filter { entry ->
            rule.matches(entry.command)
        }

        if (relevant.size < 2) return false

        // Compte les alternances rapides dans la fenêtre de durée
        val minAlternances = (durationMs / intervalMs).toInt()   // ~200
        var alternances    = 0
        var lastCommand    : CommandSpec? = null
        var lastTime       : Long = 0L

        for (entry in relevant) {
            if (lastCommand == null) {
                lastCommand = entry.command
                lastTime    = entry.timeMs
                continue
            }

            val timeDiff    = entry.timeMs - lastTime
            val isAlternate = rule.areContradictory(lastCommand!!, entry.command)
            val isFast      = timeDiff <= intervalMs * 2   // tolérance x2

            if (isAlternate && isFast) {
                alternances++
                Logger.d("ALTERNANCES ", "ALTERNE" + alternances)
                if (alternances >= minAlternances) {
                    return true
                }
            } else {
                // Séquence brisée — repart à 0
                alternances = 0
                Logger.d("ALTERNANCES ", "ALTERNE" + alternances + " IsAlter : " + isAlternate + "IsFast : " + isFast)
            }

            lastCommand = entry.command
            lastTime    = entry.timeMs
        }

        return false
    }

    fun reset() = history.clear()
}

// ── Règle de contradiction ────────────────────────────────────────────────────

data class ContradictionRule(
    val name       : String,
    val commandA   : CommandSpec,
    val commandB   : CommandSpec,
    val overflowMsg: String? = null,
) {
    fun matches(command: CommandSpec): Boolean =
        command == commandA || command == commandB

    fun areContradictory(a: CommandSpec, b: CommandSpec): Boolean =
        (a == commandA && b == commandB) || (a == commandB && b == commandA)
}

// ── Règles par défaut ─────────────────────────────────────────────────────────

fun buildContradictionRules(): List<ContradictionRule> = listOf(

    // Yeux gauche / droite
    ContradictionRule(
        name        = "OSCILLATION OCULAIRE",
        commandA    = CommandSpec("1001", "0100", "0000"),   // yeux gauche
        commandB    = CommandSpec("1001", "0011", "0000"),   // yeux droite
        overflowMsg = "INSTABILITE NEURALE CRITIQUE",
    ),

    // Ajoute ici d'autres règles selon ta config de commandes
    // ContradictionRule(
    //     name     = "CONFLIT MOTEUR",
    //     commandA = CommandSpec("XXXX", "XXXX", "XXXX"),
    //     commandB = CommandSpec("XXXX", "XXXX", "XXXX"),
    //     overflowMsg = "SURCHARGE MOTRICE",
    // ),
)