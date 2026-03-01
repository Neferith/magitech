// commonMain/kotlin/org/angelus/magitek/ui/BackOfRemoteScreen.kt

package org.angelus.magitek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.angelus.magitek.GarlemaldColors
import org.angelus.magitek.GarlemaldTheme
import org.angelus.magitek.model.*

@Composable
fun BackOfRemoteScreen(onSettingsClick: () -> Unit = {}, ) {
    val message     = remember { buildHiddenBackMessage() }
    val scrollState = rememberScrollState()

    GarlemaldTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawMetalBackground(this) },
        ) {
            Text(
                text     = "⚙",
                style    = MaterialTheme.typography.labelLarge.copy(
                    color    = GarlemaldColors.ScreenGreen.copy(alpha = 0.4f),
                    fontSize = 36.sp,
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable(onClick = onSettingsClick)
                    .padding(8.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Titre en haut, horizontal ─────────────────────────────
                EngravedText(
                    text          = message.title,
                    fontSize      = 11.sp,
                    modifier      = Modifier.fillMaxWidth(),
                    textAlign     = TextAlign.Center,
                    letterSpacing = 4.sp,
                )

                EngravedHorizontalDivider()

                // ── Codes en colonnes verticales, scrollable ───────────────
                Row(
                    modifier              = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment     = Alignment.Top,
                ) {
                    message.words.reversed().forEachIndexed { i, codes ->
                        EngravedCodeColumn(codes = codes)
                        if (i < message.words.size - 1) {
                            EngravedVerticalDivider()
                        }
                    }
                }

                EngravedHorizontalDivider()

                // ── Pied en bas, horizontal ───────────────────────────────
                EngravedText(
                    text          = "GARLEAN MAGITEK AUTHORITY  //  RESTRICTED",
                    fontSize      = 7.sp,
                    modifier      = Modifier.fillMaxWidth(),
                    textAlign     = TextAlign.Center,
                    letterSpacing = 2.sp,
                    alpha         = 0.5f,
                )
            }


        }
    }
}

// ── Colonne de codes verticaux ────────────────────────────────────────────────

@Composable
private fun EngravedCodeColumn(codes: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        codes.forEach { code ->
            EngravedText(
                text          = code,
                fontSize      = 13.sp,
                letterSpacing = 2.sp,
            )
        }
    }
}

// ── Texte gravé ───────────────────────────────────────────────────────────────

@Composable
private fun EngravedText(
    text         : String,
    fontSize     : TextUnit,
    modifier     : Modifier = Modifier,
    textAlign    : TextAlign = TextAlign.Start,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    alpha        : Float = 1f,
) {
    Text(
        text      = text,
        textAlign = textAlign,
        style     = MaterialTheme.typography.labelMedium.copy(
            fontSize      = fontSize,
            color         = Color(0xFF888888).copy(alpha = alpha),
            letterSpacing = letterSpacing,
            fontFamily    = androidx.compose.ui.text.font.FontFamily.Monospace,
            shadow        = Shadow(
                color      = Color.Black,
                offset     = Offset(1f, 1f),
                blurRadius = 0f,
            ),
        ),
        modifier = modifier,
    )
}

// ── Séparateur horizontal gravé ───────────────────────────────────────────────

@Composable
private fun EngravedHorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .drawBehind {
                drawLine(
                    color       = Color(0xFF000000),
                    start       = Offset(0f, size.height / 2f),
                    end         = Offset(size.width, size.height / 2f),
                    strokeWidth = 1f,
                )
                drawLine(
                    color       = Color(0x22FFFFFF),
                    start       = Offset(0f, size.height / 2f + 1f),
                    end         = Offset(size.width, size.height / 2f + 1f),
                    strokeWidth = 1f,
                )
            },
    )
}

// ── Séparateur vertical gravé ─────────────────────────────────────────────────

@Composable
private fun EngravedVerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(4.dp)
            .drawBehind {
                drawLine(
                    color       = Color(0xFF000000),
                    start       = Offset(size.width / 2f, 0f),
                    end         = Offset(size.width / 2f, size.height),
                    strokeWidth = 1f,
                )
                drawLine(
                    color       = Color(0x22FFFFFF),
                    start       = Offset(size.width / 2f + 1f, 0f),
                    end         = Offset(size.width / 2f + 1f, size.height),
                    strokeWidth = 1f,
                )
            },
    )
}

// ── Fond métal brossé ─────────────────────────────────────────────────────────

private fun drawMetalBackground(scope: DrawScope) {
    with(scope) {
        drawRect(color = Color(0xFF1A1A1A))

        val lineColor = Color(0x08FFFFFF)
        var y = 0f
        while (y < size.height) {
            drawLine(
                color       = lineColor,
                start       = Offset(0f, y),
                end         = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += 3f
        }

        drawRect(
            brush = Brush.radialGradient(
                colors  = listOf(Color.Transparent, Color(0x99000000)),
                center  = Offset(size.width / 2f, size.height / 2f),
                radius  = size.width * 0.8f,
            ),
        )

        val screwRadius = 6f
        val margin      = 14f
        listOf(
            Offset(margin, margin),
            Offset(size.width - margin, margin),
            Offset(margin, size.height - margin),
            Offset(size.width - margin, size.height - margin),
        ).forEach { center ->
            drawCircle(color = Color(0xFF2A2A2A), radius = screwRadius, center = center)
            drawCircle(color = Color(0xFF444444), radius = screwRadius, center = center, style = Stroke(1f))
            drawLine(
                color       = Color(0xFF111111),
                start       = Offset(center.x - screwRadius * 0.6f, center.y),
                end         = Offset(center.x + screwRadius * 0.6f, center.y),
                strokeWidth = 1.5f,
            )
        }
    }
}