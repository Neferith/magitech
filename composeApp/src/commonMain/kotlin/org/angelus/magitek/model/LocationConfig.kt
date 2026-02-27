// commonMain/kotlin/org/angelus/magitek/model/LocationConfig.kt

package org.angelus.magitek.model

data class LocationConfig(
    val id       : String,
    val name     : String,
    val frequencies: List<ActivationFrequency>,
)

fun buildLocations(): List<LocationConfig> = listOf(
    LocationConfig(
        id   = "NONE",
        name = "Aucun",
        frequencies = emptyList(),
    ),
    LocationConfig(
        id   = "LIMSA",
        name = "Limsa Lominsa",
        frequencies = listOf(
            ActivationFrequency(name = "ÉLÉONORE",   target = 42000L,  tolerance = 400L),
            ActivationFrequency(name = "RÉSONNANCE", target = 131071L, tolerance = 600L),
        ),
    ),
    LocationConfig(
        id   = "GRIDANIA",
        name = "Gridania",
        frequencies = listOf(
            ActivationFrequency(name = "FLUX SYLVESTRE", target = 78000L,  tolerance = 500L),
            ActivationFrequency(name = "PROTOCOLE",      target = 210000L, tolerance = 400L),
        ),
    ),
    LocationConfig(
        id   = "ULDAH",
        name = "Ul'dah",
        frequencies = listOf(
            ActivationFrequency(name = "RÉSONNANCE",  target = 95000L,  tolerance = 300L),
            ActivationFrequency(name = "SIGNAL DEEP", target = 180000L, tolerance = 500L),
        ),
    ),
    LocationConfig(
        id   = "ISHGARD",
        name = "Ishgard",
        frequencies = listOf(
            ActivationFrequency(name = "ÉTHER GLACIAL", target = 55000L,  tolerance = 600L),
            ActivationFrequency(name = "PROTOCOLE",     target = 210000L, tolerance = 400L),
        ),
    ),
    LocationConfig(
        id   = "GARLEMALD",
        name = "Garlemald",
        frequencies = listOf(
            ActivationFrequency(name = "ÉLÉONORE",    target = 42000L,  tolerance = 400L),
            ActivationFrequency(name = "IMPÉRIAL",    target = 131071L, tolerance = 800L),
            ActivationFrequency(name = "PROTOCOLE",   target = 210000L, tolerance = 400L),
            ActivationFrequency(name = "EXPÉRIMENTAL",target = 250000L, tolerance = 300L),
        ),
    ),
)

fun List<LocationConfig>.findById(id: String): LocationConfig =
    firstOrNull { it.id == id } ?: first()