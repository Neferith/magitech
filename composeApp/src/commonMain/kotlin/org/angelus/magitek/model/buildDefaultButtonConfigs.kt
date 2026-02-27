// commonMain/kotlin/org/angelus/magitek/model/DefaultButtonConfigs.kt

package org.angelus.magitek.model

/**
 * Configuration par défaut des 64 boutons.
 * Organisée par module, les commandes les plus utiles en RP en premier.
 */
fun buildDefaultButtonConfigs(): Map<Int, ButtonConfig> {

    data class Cmd(val mod: String, val sub: String, val act: String, val label: String)

    val defaults: List<Cmd> = listOf(

        // ── Psychologique (0100) — boutons 0-11 ──────────────────────────────
        Cmd("0100", "0000", "0000", "JOI_FAI"),   // 0  Joie / faible
        Cmd("0100", "0000", "0010", "JOI_FOR"),   // 1  Joie / forte
        Cmd("0100", "0001", "0000", "TRI_FAI"),   // 2  Tristesse / faible
        Cmd("0100", "0001", "0010", "TRI_FOR"),   // 3  Tristesse / forte
        Cmd("0100", "0010", "0000", "COL_FAI"),   // 4  Colère / faible
        Cmd("0100", "0010", "0010", "COL_FOR"),   // 5  Colère / forte
        Cmd("0100", "0011", "0000", "PEU_FAI"),   // 6  Peur / faible
        Cmd("0100", "1101", "0000", "ANX_FAI"),   // 7  Anxiété / faible
        Cmd("0100", "1110", "0000", "MEM_FAI"),   // 8  Mémoire / faible
        Cmd("0100", "1111", "0000", "NEU_FAI"),   // 9  Neutralité / faible
        Cmd("0100", "0110", "0000", "AMO_FAI"),   // 10 Amour / faible
        Cmd("0100", "1010", "0110", "CUL_SUP"),   // 11 Culpabilité / supprimer

        // ── Communication vocale (1001) — boutons 12-15 ──────────────────────
        Cmd("1001", "0000", "0000", "VOC_NRM"),   // 12 Parole normale / autoriser
        Cmd("1001", "0000", "0001", "VOC_BLK"),   // 13 Parole normale / bloquer temp
        Cmd("1001", "0100", "0000", "SIL_AUT"),   // 14 Silence forcé / autoriser
        Cmd("1001", "0011", "0000", "ENC_AUT"),   // 15 Parole encodée / autoriser

        // ── Tête (1100) — boutons 16-23 ──────────────────────────────────────
        Cmd("1100", "0000", "0000", "TET_GAU"),   // 16 Tourner gauche
        Cmd("1100", "0001", "0000", "TET_DRO"),   // 17 Tourner droite
        Cmd("1100", "0100", "0000", "HOC_OUI"),   // 18 Hocher oui
        Cmd("1100", "0101", "0000", "SEC_NON"),   // 19 Secouer non
        Cmd("1100", "0010", "0000", "INC_GAU"),   // 20 Incliner gauche
        Cmd("1100", "0011", "0000", "INC_DRO"),   // 21 Incliner droite
        Cmd("1100", "0111", "0000", "PEN_AVA"),   // 22 Pencher avant
        Cmd("1100", "1000", "0000", "PEN_ARR"),   // 23 Pencher arrière

        // ── Yeux (1011) — boutons 24-31 ──────────────────────────────────────
        Cmd("1011", "0000", "0000", "YEU_CLI"),   // 24 Cligner
        Cmd("1011", "0001", "0000", "YEU_FOC"),   // 25 Focuser
        Cmd("1011", "0010", "0000", "YEU_GAU"),   // 26 Regarder gauche
        Cmd("1011", "0011", "0000", "YEU_DRO"),   // 27 Regarder droite
        Cmd("1011", "0100", "0000", "YEU_HAU"),   // 28 Regarder haut
        Cmd("1011", "0101", "0000", "YEU_BAS"),   // 29 Regarder bas
        Cmd("1011", "0110", "0000", "YEU_SCN"),   // 30 Scanner
        Cmd("1011", "0111", "0000", "YEU_FIX"),   // 31 Fixer

        // ── Bras (1010) — boutons 32-39 ──────────────────────────────────────
        Cmd("1010", "0000", "0000", "BRS_INA"),   // 32 Inactif
        Cmd("1010", "0001", "0000", "BRS_LGA"),   // 33 Lever bras gauche
        Cmd("1010", "0010", "0000", "BRS_LDR"),   // 34 Lever bras droit
        Cmd("1010", "0011", "0000", "BRS_SAI"),   // 35 Saisir
        Cmd("1010", "0100", "0000", "BRS_REL"),   // 36 Relâcher
        Cmd("1010", "0101", "0000", "BRS_POU"),   // 37 Pousser
        Cmd("1010", "0110", "0000", "BRS_TIR"),   // 38 Tirer
        Cmd("1010", "0111", "0000", "BRS_ROT"),   // 39 Rotation bras

        // ── Jambes (1000) — boutons 40-47 ────────────────────────────────────
        Cmd("1000", "0000", "0000", "JAM_INA"),   // 40 Inactif
        Cmd("1000", "0001", "0000", "JAM_AVA"),   // 41 Avancer
        Cmd("1000", "0010", "0000", "JAM_REC"),   // 42 Reculer
        Cmd("1000", "0110", "0010", "JAM_COU"),   // 43 Courir
        Cmd("1000", "0111", "0000", "JAM_LEN"),   // 44 Marcher lentement
        Cmd("1000", "0101", "0000", "JAM_SAU"),   // 45 Sauter
        Cmd("1000", "1000", "0000", "JAM_LGA"),   // 46 Latéral gauche
        Cmd("1000", "1001", "0000", "JAM_LDR"),   // 47 Latéral droit

        // ── Respiration (1101) — boutons 48-55 ───────────────────────────────
        Cmd("1101", "0000", "0000", "RES_INS"),   // 48 Inspirer
        Cmd("1101", "0001", "0000", "RES_EXP"),   // 49 Expirer
        Cmd("1101", "0010", "0000", "RES_PAC"),   // 50 Pause courte
        Cmd("1101", "0011", "0000", "RES_PAL"),   // 51 Pause longue
        Cmd("1101", "0100", "0000", "RES_RAP"),   // 52 Rythme rapide
        Cmd("1101", "0101", "0000", "RES_LEN"),   // 53 Rythme lent
        Cmd("1101", "1000", "0000", "RES_PRO"),   // 54 Respiration profonde
        Cmd("1101", "0110", "0000", "RES_BLK"),   // 55 Bloquer inspiration

        // ── Flux interne (1110) — boutons 56-59 ──────────────────────────────
        Cmd("1110", "0000", "0000", "FLX_SYN"),   // 56 Synchroniser flux
        Cmd("1110", "0001", "0000", "FLX_AMP"),   // 57 Amplifier flux
        Cmd("1110", "0010", "0000", "FLX_BLK"),   // 58 Bloquer flux
        Cmd("1110", "0011", "0000", "FLX_DET"),   // 59 Détecter flux

        // ── Expérimental (1111) — boutons 60-63 ──────────────────────────────
        Cmd("1111", "0000", "0000", "EXP_00"),    // 60
        Cmd("1111", "0001", "0000", "EXP_01"),    // 61
        Cmd("1111", "0010", "0000", "EXP_02"),    // 62
        Cmd("1111", "0011", "0000", "EXP_03"),    // 63
    )

    return defaults.mapIndexed { index, (mod, sub, act, label) ->
        index to ButtonConfig(
            buttonIndex = index,
            assignment  = ButtonAssignment.SingleCommand(
                CommandSpec(
                    moduleCode    = mod,
                    submoduleCode = sub,
                    actionCode    = act,
                )
            ),
            customLabel = label,
        )
    }.toMap()
}