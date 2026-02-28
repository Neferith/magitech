// commonMain/kotlin/org/angelus/magitek/ui/FrequencyDial.kt

package org.angelus.magitek.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import org.angelus.magitek.GarlemaldColors
import kotlin.math.*

// Plage : 0 .. 262143 (18 bits = 2^18 - 1)
private const val MAX_FREQ = 262143L

/**
 * Molette crantée style Garlemald.
 * - Drag circulaire → rotation → changement de fréquence
 * - Les crans cliquent visuellement à chaque pas
 */
@Composable
fun FrequencyDial(
    frequency   : Long,
    onChange    : (Long) -> Unit,
    onDragStart : () -> Unit = {},
    onDragEnd   : () -> Unit = {},
    onDetent    : () -> Unit = {},
    modifier    : Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    // Ajouter avec les autres remember dans FrequencyDial :
    var lastDetentFreq by remember { mutableStateOf(frequency) }
    val detentStep = 512L   // un cran tous les 512 unités (~0.2% de la plage)

    // Angle accumulé (en radians) — on en déduit la fréquence
    // 1 tour complet = MAX_FREQ / 8  (8 tours pour parcourir toute la plage)
    var angleRad by remember { mutableStateOf(
        frequency.toDouble() / MAX_FREQ * 8.0 * 2.0 * PI
    ) }

    // Centre du canvas pour calculer l'angle du drag
    var centerX by remember { mutableStateOf(0f) }
    var centerY by remember { mutableStateOf(0f) }
    var lastAngle by remember { mutableStateOf(0.0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Label gauche ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text  = "FREQ",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 9.sp),
                )
                Text(
                    text  = frequency.toString(2).padStart(18, '0').chunked(6).joinToString(" "),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize      = 8.sp,
                        color         = GarlemaldColors.ScreenGreenDim,
                        letterSpacing = 1.sp,
                    ),
                )
                Text(
                    text  = frequency.toString().padStart(6, '0'),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 10.sp,
                        color    = GarlemaldColors.ScreenGreen,
                    ),
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Roue crantée ─────────────────────────────────────────────────
            Canvas(
                modifier = Modifier
                    .size(76.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onDragStart()
                                centerX   = size.width  / 2f
                                centerY   = size.height / 2f
                                lastAngle = atan2(
                                    (offset.y - centerY).toDouble(),
                                    (offset.x - centerX).toDouble(),
                                )
                            },
                            onDragEnd   = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, _ ->
                                val newAngle = atan2(
                                    (change.position.y - centerY).toDouble(),
                                    (change.position.x - centerX).toDouble(),
                                )
                                var delta = newAngle - lastAngle
                                // Correction saut -π/+π
                                if (delta > PI)  delta -= 2 * PI
                                if (delta < -PI) delta += 2 * PI

                                angleRad += delta
                                lastAngle = newAngle

                                val newFreq = ((angleRad / (8.0 * 2.0 * PI)) * MAX_FREQ)
                                    .toLong()
                                    .coerceIn(0L, MAX_FREQ)
                                onChange(newFreq)
                                // Cran franchi ?
                                if (kotlin.math.abs(newFreq - lastDetentFreq) >= detentStep) {
                                    lastDetentFreq = (newFreq / detentStep) * detentStep
                                    onDetent()
                                }
                            },
                        )
                    },
            ) {
                val cx     = size.width  / 2f
                val cy     = size.height / 2f
                val radius = size.minDimension / 2f - 4f
                val rotation = angleRad.toFloat()

                drawCogWheel(
                    cx       = cx,
                    cy       = cy,
                    radius   = radius,
                    rotation = rotation,
                    textMeasurer = textMeasurer,
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Indicateur droit ──────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = "0x${frequency.toString(16).uppercase().padStart(5, '0')}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 10.sp,
                        color    = GarlemaldColors.ScreenGreen,
                    ),
                )
                Text(
                    text  = "${(frequency.toDouble() / MAX_FREQ * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 9.sp,
                        color    = GarlemaldColors.ScreenGreenDim,
                    ),
                )
                Text(
                    text  = "RÉSONATEUR",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 8.sp),
                )
            }
        }
    }
}

// ── Dessin de la roue crantée ─────────────────────────────────────────────────

private fun DrawScope.drawCogWheel(
    cx          : Float,
    cy          : Float,
    radius      : Float,
    rotation    : Float,
    textMeasurer: TextMeasurer,
) {
    val toothCount  = 16
    val innerRadius = radius * 0.72f
    val toothDepth  = radius - innerRadius
    val hubRadius   = radius * 0.22f

    // Ombre portée
    drawCircle(
        color  = Color(0xFF000000),
        radius = radius + 2f,
        center = Offset(cx + 2f, cy + 2f),
        alpha  = 0.5f,
    )

    // Corps principal — dégradé métal sombre
    drawCircle(
        brush  = Brush.radialGradient(
            colors  = listOf(
                GarlemaldColors.MetalDark,
                Color(0xFF222222),
                Color(0xFF0D0D0D),
            ),
            center  = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
            radius  = radius * 1.2f,
        ),
        radius = innerRadius,
        center = Offset(cx, cy),
    )

    // Bordure corps
    drawCircle(
        color       = GarlemaldColors.MetalDark,
        radius      = innerRadius,
        center      = Offset(cx, cy),
        style       = Stroke(width = 1.5f),
    )

    // Crans (dents)
    val path = Path()
    for (i in 0 until toothCount) {
        val angleStep  = (2 * PI / toothCount).toFloat()
        val toothAngle = i * angleStep + rotation
        val halfTooth  = angleStep * 0.28f

        val x0 = cx + innerRadius * cos(toothAngle - halfTooth)
        val y0 = cy + innerRadius * sin(toothAngle - halfTooth)
        val x1 = cx + radius * cos(toothAngle - halfTooth * 0.5f)
        val y1 = cy + radius * sin(toothAngle - halfTooth * 0.5f)
        val x2 = cx + radius * cos(toothAngle + halfTooth * 0.5f)
        val y2 = cy + radius * sin(toothAngle + halfTooth * 0.5f)
        val x3 = cx + innerRadius * cos(toothAngle + halfTooth)
        val y3 = cy + innerRadius * sin(toothAngle + halfTooth)

        if (i == 0) path.moveTo(x0, y0)
        else        path.lineTo(x0, y0)

        path.lineTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
    }
    path.close()

    // Fill dents — métal plus clair
    drawPath(
        path  = path,
        brush = Brush.radialGradient(
            colors  = listOf(GarlemaldColors.MetalDark, Color(0xFF1A1A1A)),
            center  = Offset(cx, cy),
            radius  = radius,
        ),
    )
    // Contour dents
    drawPath(
        path  = path,
        color = GarlemaldColors.MetalLight.copy(alpha = 0.4f),
        style = Stroke(width = 1f),
    )

    // Rainures radiales (marqueurs de position)
    for (i in 0 until 8) {
        val angle = i * (PI / 4).toFloat() + rotation
        drawLine(
            color       = GarlemaldColors.MetalDark,
            start       = Offset(cx + hubRadius * 1.5f * cos(angle), cy + hubRadius * 1.5f * sin(angle)),
            end         = Offset(cx + innerRadius * 0.85f * cos(angle), cy + innerRadius * 0.85f * sin(angle)),
            strokeWidth = 1f,
        )
    }

    // Moyeu central
    drawCircle(
        brush  = Brush.radialGradient(
            colors  = listOf(Color(0xFF2A2A2A), Color(0xFF0A0A0A)),
            center  = Offset(cx, cy),
            radius  = hubRadius,
        ),
        radius = hubRadius,
        center = Offset(cx, cy),
    )
    drawCircle(
        color       = GarlemaldColors.ImperialRed.copy(alpha = 0.6f),
        radius      = hubRadius,
        center      = Offset(cx, cy),
        style       = Stroke(width = 1f),
    )

    // Point indicateur sur le moyeu (tourne avec la roue)
    val dotAngle = rotation
    val dotX     = cx + hubRadius * 0.55f * cos(dotAngle)
    val dotY     = cy + hubRadius * 0.55f * sin(dotAngle)
    drawCircle(
        color  = GarlemaldColors.ImperialRed,
        radius = 2.5f,
        center = Offset(dotX, dotY),
    )
}