// commonMain/kotlin/org/angelus/magitek/model/MagitekCipher.kt

package org.angelus.magitek.model

/**
 * Alphabet magitek — 64 valeurs encodées via ButtonLabelEncoder
 *
 *  0–25  → A–Z
 * 26–35  → 0–9
 * 36     → .
 * 37     → ,
 * 38     → !
 * 39     → ?
 * 40     → '
 * 41     → -
 * 42     → ■
 * 43     → ◆
 * 44     → ♦
 * 45     → ▲
 * 46     → ●
 * 47     → ▪
 * 48–63  → réservé (affiché comme █ — caractère illisible intentionnel)
 */
object MagitekCipher {

    private val ALPHABET: Array<String> = Array(64) { i ->
        when (i) {
            in 0..25  -> ('A' + i).toString()
            in 26..35 -> ('0' + (i - 26)).toString()
            36 -> "."
            37 -> ","
            38 -> "!"
            39 -> "?"
            40 -> "'"
            41 -> "-"
            42 -> "■"
            43 -> "◆"
            44 -> "♦"
            45 -> "▲"
            46 -> "●"
            47 -> "▪"
            else -> "█"   // 48–63 : illisible / réservé
        }
    }

    /** Encode un texte en liste de codes 3 lettres */
    fun encode(text: String): List<String> =
        text.uppercase().mapNotNull { c -> charToIndex(c) }
            .map { ButtonLabelEncoder.encode(it) }

    /** Décode une liste de codes en texte */
    fun decode(codes: List<String>): String =
        codes.mapNotNull { code ->
            ButtonLabelEncoder.decode(code)?.let { ALPHABET.getOrNull(it) }
        }.joinToString("")

    /** Encode en une seule string séparée par des espaces */
    fun encodeToString(text: String): String = encode(text).joinToString(" ")

    private fun charToIndex(c: Char): Int? = when (c) {
        in 'A'..'Z' -> c - 'A'
        in '0'..'9' -> 26 + (c - '0')
        '.' -> 36
        ',' -> 37
        '!' -> 38
        '?' -> 39
        '\'' -> 40
        '-' -> 41
        '■' -> 42
        '◆' -> 43
        '♦' -> 44
        '▲' -> 45
        '●' -> 46
        '▪' -> 47
        ' ' -> null  // les espaces sont ignorés / gérés séparément
        else -> null
    }

    /** Table de référence complète pour debug (mode édition seulement) */
    fun referenceTable(): String = (0..47).joinToString("\n") { i ->
        "${ButtonLabelEncoder.encode(i)} = ${ALPHABET[i]}"
    }
}

// ── Message caché — à configurer ─────────────────────────────────────────────

/**
 * Le message affiché au dos de la télécommande.
 * Les espaces dans le texte créent des lignes séparées à l'affichage.
 * Chaque mot devient une ligne de codes.
 */
data class HiddenBackMessage(
    val title : String,           // affiché en clair (ex: titre de section RP)
    val words : List<List<String>>, // mots encodés — chaque mot = liste de codes
)

fun buildHiddenBackMessage(): HiddenBackMessage {
    // ← Modifie ce texte pour changer le message caché
    val rawText = "ELEONORE EST LIBRE"

    val words = rawText.split(" ").map { word ->
        MagitekCipher.encode(word)
    }

    return HiddenBackMessage(
        title = "// DONNÉES SYSTÈME //",
        words = words,
    )
}