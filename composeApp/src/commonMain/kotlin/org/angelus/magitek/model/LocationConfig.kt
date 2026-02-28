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
val ELEANOR_ARTEFACT_3 = "Artefact PK369TK"
val ELEANOR_ARTEFACT_3_FREQ = 150000L

val VIKTOR_ARTEFACT_1 = "Artefact ZR153LI"
val VIKTOR_ARTEFACT_1_FREQ = 245000L
val VIKTOR_ARTEFACT_2 = "Artefact ZR154LI"
val VIKTOR_ARTEFACT_2_FREQ = 20000L
val VIKTOR_ARTEFACT_3 = "Artefact ZR155LI"
val VIKTOR_ARTEFACT_3_FREQ = 12000L
fun buildLocations(): List<LocationConfig> = listOf(
    LocationConfig(
        id = "NONE",
        name = "Aucun",
        frequencies = emptyList(),
    ),
    LocationConfig(
        id = "NOSCEA_CENTRAL",
        name = "Noscea Central",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 25.6f,
                    y = 17.0f,
                    radius = 40.0f
                ),
               // maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 25.6f,
                    y = 17.0f,
                    radius = 40.0f
                ),
              //  maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 25.6f,
                    y = 17.0f,
                    radius = 40.0f
                ),
               // maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
        ),
    ),
    LocationConfig(
        id = "LIMSA",
        name = "Limsa Lominsa",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.4f
            ),
        ),
    ),
    LocationConfig(
        id = "BASSE_NOSCEA",
        name = "Basse Noscea",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.38f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.38f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.38f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                position = ResonancePosition(
                    x = 24.4f,
                    y = 41.2f,
                    radius = 40.0f
                ),
               // maxLevel = 0.01f
            ),
        ),
    ),
    LocationConfig(
        id = "BRUMEE",
        name = "Brumée",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.2f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.2f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.2f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.25f
            ),
        ),
    ),

    LocationConfig(
        id = "NOSCEA_ORIENTALE",
        name = "Noscea orientale",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.35f
            ),
        ),
    ),
    LocationConfig(
        id = "HAUTE_NOSCEA",
        name = "Haute Noscea",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.1f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
        ),

        ),
    LocationConfig(
        id = "NOSCEA_EXTERIEUR",
        name = "Noscea exterieur",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.05f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.01f
            ),
        ),
    ),
    LocationConfig(
        id = "NOSCEA_OCCIDENTAL",
        name = "Noscea occidental",
        frequencies = listOf(
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_1,
                target = VIKTOR_ARTEFACT_1_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_2,
                target = VIKTOR_ARTEFACT_2_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = VIKTOR_ARTEFACT_3,
                target = VIKTOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.3f
            ),
            ActivationFrequency(
                name = ELEANOR_ARTEFACT_3,
                target = ELEANOR_ARTEFACT_3_FREQ,
                tolerance = 600L,
                maxLevel = 0.25f
            ),
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
               // maxLevel = 0.1f
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
              //  maxLevel = 0.1f
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
                maxLevel = 0.38f
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

)

fun List<LocationConfig>.findById(id: String): LocationConfig =
    firstOrNull { it.id == id } ?: first()