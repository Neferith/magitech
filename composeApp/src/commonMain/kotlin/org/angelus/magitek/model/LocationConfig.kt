// commonMain/kotlin/org/angelus/magitek/model/LocationConfig.kt

package org.angelus.magitek.model

data class LocationConfig(
    val id: String,
    val name: String,
    val frequencies: List<ActivationFrequency>,
)

val ELEANOR_ARTEFACT_1 = "Artefact PK367TK"
val ELEANOR_ARTEFACT_1_FREQ = 55000L
val ELEANOR_ARTEFACT_2 = "Artefact PK368TK"
val ELEANOR_ARTEFACT_2_FREQ = 5000L
val ELEANOR_ARTEFACT_3 = "Artefact PK368TK"

fun buildLocations(): List<LocationConfig> = listOf(
    LocationConfig(
        id = "NONE",
        name = "Aucun",
        frequencies = emptyList(),
    ),
    LocationConfig(
        id = "LIMSA",
        name = "Limsa Lominsa",
        frequencies = listOf(
            ActivationFrequency(name = "ÉLÉONORE", target = 42000L, tolerance = 400L),
            ActivationFrequency(name = "RÉSONNANCE", target = 131071L, tolerance = 600L),
        ),
    ),
    LocationConfig(
        id = "GRIDANIA",
        name = "Gridania",
        frequencies = listOf(
            ActivationFrequency(name = "FLUX SYLVESTRE", target = 78000L, tolerance = 500L),
            ActivationFrequency(name = "PROTOCOLE", target = 210000L, tolerance = 400L),
        ),
    ),
    LocationConfig(
        id = "ULDAH",
        name = "Ul'dah",
        frequencies = listOf(
            ActivationFrequency(name = "RÉSONNANCE", target = 95000L, tolerance = 300L),
            ActivationFrequency(name = "SIGNAL DEEP", target = 180000L, tolerance = 500L),
        ),
    ),
    LocationConfig(
        id = "EMPYREE",
        name = "Empyrée",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 13.9f,
                    y = 9.9f,
                    radius = 40.0f
                ),
                maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 13.9f,
                    y = 9.9f,
                    radius = 40.0f
                ),
                maxLevel = 0.1f
            ),
        ),
    ),
    LocationConfig(
        id = "ISHGARD",
        name = "Ishgard - L'Assise",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
        ),
    ),
    LocationConfig(
        id = "ISHGARD_ASSISE",
        name = "Ishgard - L'Assise",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.38f
            ),
        ),
    ),
    LocationConfig(
        id = "ISHGARD_CONTREFORTS",
        name = "Ishgard - Les Contreforts",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
        ),
    ),
    LocationConfig(
        id = "COERTHAS_CENTRAL",
        name = "Coerthas central",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.15f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.15f
            ),
        ),
    ),
    LocationConfig(
        id = "COERTHAS_OCCIDENTAL",
        name = "Coerthas occidental",
        frequencies = listOf(
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_1,
                target = ELEANOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_2,
                target = ELEANOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
        ),
    ),
    LocationConfig(
        id = "GARLEMALD",
        name = "Garlemald",
        frequencies = listOf(),
    ),
)

fun List<LocationConfig>.findById(id: String): LocationConfig =
    firstOrNull { it.id == id } ?: first()