package org.angelus.magitek.model

// ── État d'overflow ───────────────────────────────────────────────────────────

private const val DEFAULT_OVERFLOW_MESSAGE = "SURCHARGE SYSTEME CRITIQUE"

data class OverflowState(
    val isActive    : Boolean          = false,
    val message     : String           = DEFAULT_OVERFLOW_MESSAGE,
    val encodedWords: List<List<String>> = encodeMessage(DEFAULT_OVERFLOW_MESSAGE),
)

private fun encodeMessage(text: String): List<List<String>> =
    text.split(" ").map { word -> MagitekCipher.encode(word) }

fun buildOverflowState(message: String?): OverflowState {
    val msg = message ?: DEFAULT_OVERFLOW_MESSAGE
    return OverflowState(
        isActive     = true,
        message      = msg,
        encodedWords = encodeMessage(msg),
    )
}