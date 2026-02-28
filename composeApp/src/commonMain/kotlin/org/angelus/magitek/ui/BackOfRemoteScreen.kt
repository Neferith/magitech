// commonMain/kotlin/org/angelus/magitek/ui/BackOfRemoteScreen.kt

package org.angelus.magitek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.angelus.magitek.GarlemaldColors
import org.angelus.magitek.GarlemaldTheme
import org.angelus.magitek.drawScanlines
import org.angelus.magitek.model.*

@Composable
fun BackOfRemoteScreen(
    isEditMode: Boolean = false,
) {
    val message = remember { buildHiddenBackMessage() }

    GarlemaldTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = GarlemaldColors.Background,
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── En-tête ───────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GarlemaldColors.SurfaceVariant)
                        .border(1.dp, GarlemaldColors.ImperialRedDark)
                        .padding(10.dp),
                ) {
                    Text(
                        text     = message.title,
                        style    = MaterialTheme.typography.titleMedium.copy(fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }

                // ── Corps du message encodé ───────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(GarlemaldColors.ScreenBackground)
                        .border(2.dp, GarlemaldColors.ScreenGreenDim)
                        .drawBehind { drawScanlines(this) }
                        .padding(16.dp),
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        message.words.forEachIndexed { wordIndex, codes ->
                            WordRow(
                                wordIndex  = wordIndex,
                                codes      = codes,
                                isEditMode = isEditMode,
                            )
                        }
                    }
                }

                // ── Pied — indication de rotation ────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "↺  PIVOTER POUR REVENIR  ↻",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize      = 8.sp,
                            color         = GarlemaldColors.MetalDark,
                            letterSpacing = 2.sp,
                        ),
                    )
                }
            }
        }
    }
}

// ── Ligne d'un mot encodé ─────────────────────────────────────────────────────

@Composable
private fun WordRow(
    wordIndex : Int,
    codes     : List<String>,
    isEditMode: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Index du mot en guise de "numéro de ligne"
        Text(
            text  = "> ${(wordIndex + 1).toString().padStart(2, '0')}",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 8.sp,
                color    = GarlemaldColors.ScreenGreenDim,
            ),
        )

        // Codes des caractères
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            codes.forEach { code ->
                CodeChip(code = code, isEditMode = isEditMode)
            }
        }
    }
}

// ── Puce d'un code ────────────────────────────────────────────────────────────

@Composable
private fun CodeChip(
    code      : String,
    isEditMode: Boolean,
) {
    val decoded = remember(code) {
        val index = ButtonLabelEncoder.decode(code)
        if (index != null) MagitekCipher.decode(listOf(code)) else "?"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Code 3 lettres
        Box(
            modifier = Modifier
                .background(GarlemaldColors.SurfaceVariant)
                .border(1.dp, GarlemaldColors.ScreenGreenDim)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = code,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize      = 11.sp,
                    color         = GarlemaldColors.ScreenGreen,
                    letterSpacing = 1.sp,
                ),
            )
        }

        // Caractère décodé — visible uniquement en mode édition
        if (isEditMode) {
            Text(
                text  = decoded,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 9.sp,
                    color    = GarlemaldColors.ImperialRed,
                ),
            )
        }
    }
}