import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import org.angelus.magitek.GarlemaldColors
import org.angelus.magitek.GarlemaldTheme
import org.angelus.magitek.rememberFeedbackController

// ── Modèle ────────────────────────────────────────────────────────────────────

data class MagitekCommand(
    val label: String,
    val bits64: Long,           // valeur encodée sur 64 bits
    val description: String = "",
)

// Génération de 64 commandes exemple (à remplacer par tes vraies specs)
fun buildDefaultCommands(): List<MagitekCommand> {
    val labels = listOf(
        "PWR", "RST", "ACT", "MOV", "ATK", "DEF", "SHD", "SYN",
        "FRQ", "AMP", "GAI", "MOD", "EXT", "INT", "PLS", "WAV",
        "T01", "T02", "T03", "T04", "T05", "T06", "T07", "T08",
        "C01", "C02", "C03", "C04", "C05", "C06", "C07", "C08",
        "LCK", "ULK", "PRG", "RUN", "STP", "NUL", "ERR", "DBG",
        "S01", "S02", "S03", "S04", "S05", "S06", "S07", "S08",
        "ALT", "OVR", "INV", "JAM", "SCN", "POS", "TRK", "HLT",
        "X01", "X02", "X03", "X04", "X05", "X06", "X07", "X08",
    )
    return labels.mapIndexed { i, label ->
        // Structure 64 bits : [18 bits fréquence][12 bits commande][34 bits réservés]
        val freq    = ((i * 137 + 42) and 0x3FFFF).toLong()       // 18 bits fréquence custom
        val cmd     = ((i * 13 + 7)   and 0xFFF).toLong()         // 12 bits commande
        val reserved = 0L                                          // 34 bits (mystérieux)
        val bits64  = (freq shl 46) or (cmd shl 34) or reserved
        MagitekCommand(label = label, bits64 = bits64)
    }
}

// ── Formatage binaire 64 bits ─────────────────────────────────────────────────

fun Long.toDisplay64Bits(): String {
    val bin = java.lang.Long.toBinaryString(this).padStart(64, '0')
    // Découpage visuel : [18][12][34]
    return buildString {
        append(bin.substring(0, 18))
        append(" | ")
        append(bin.substring(18, 30))
        append(" | ")
        append(bin.substring(30, 64))
    }
}

fun Long.toHex16(): String = java.lang.Long.toHexString(this)
    .uppercase()
    .padStart(16, '0')

// ── Écran principal ───────────────────────────────────────────────────────────

@Composable
fun MagitekRemoteScreen() {
    val commands = remember { buildDefaultCommands() }
    var selectedCommand by remember { mutableStateOf<MagitekCommand?>(null) }

    // Feedback son + vibration
    val feedback = rememberFeedbackController()
    DisposableEffect(Unit) {
        onDispose { feedback.release() }
    }


    GarlemaldTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = GarlemaldColors.Background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ImperialHeader()

                CommandScreen(command = selectedCommand)

                ButtonGrid(
                    commands  = commands,
                    selected  = selectedCommand,
                    onSelect  = { cmd ->
                        selectedCommand = cmd
                        feedback.triggerCommandFeedback()   // ← déclenchement
                    },
                    modifier  = Modifier.weight(1f),
                )

                StatusBar()
            }
        }
    }
}

// ── En-tête ───────────────────────────────────────────────────────────────────

@Composable
fun ImperialHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .border(1.dp, GarlemaldColors.ImperialRedDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = "UNITÉ MAGITEK — TÉLÉCOMMANDE v3.7",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 10.sp),
        )
        DiodeIndicator()
    }
}

@Composable
fun DiodeIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "diode")
    val alpha by infiniteTransition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "diode_alpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                GarlemaldColors.DiodRed.copy(alpha = alpha),
                RoundedCornerShape(50),
            )
            .border(1.dp, GarlemaldColors.DiodRed.copy(alpha = 0.5f), RoundedCornerShape(50)),
    )
}

// ── Écran de commande ─────────────────────────────────────────────────────────

@Composable
fun CommandScreen(command: MagitekCommand?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(GarlemaldColors.ScreenBackground)
            .border(2.dp, GarlemaldColors.ScreenGreenDim)
            .drawBehind { drawScanlines(this) }
            .padding(10.dp),
    ) {
        if (command == null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "> ATTENTE COMMANDE...",
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text  = "> _",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = GarlemaldColors.ScreenGreenDim,
                    ),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "> CMD: ${command.label}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = GarlemaldColors.ScreenGreen,
                    ),
                )
                Text(
                    text  = "> HEX: 0x${command.bits64.toHex16()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 11.sp,
                        color    = GarlemaldColors.ScreenGreenDim,
                    ),
                )
                Text(
                    text  = "> BIN: ${command.bits64.toDisplay64Bits()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 8.sp,
                        color    = GarlemaldColors.ScreenGreenDim,
                        letterSpacing = 0.5.sp,
                    ),
                    maxLines  = 2,
                    overflow  = TextOverflow.Clip,
                )
            }
        }
    }
}

// Scanlines — effet CRT léger
fun drawScanlines(scope: DrawScope) {
    val lineHeight = 4f
    val lineColor  = Color(0x0A00FF88)
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(
            color       = lineColor,
            start       = Offset(0f, y),
            end         = Offset(scope.size.width, y),
            strokeWidth = 1.5f,
        )
        y += lineHeight
    }
}

// ── Grille de boutons ─────────────────────────────────────────────────────────

@Composable
fun ButtonGrid(
    commands : List<MagitekCommand>,
    selected : MagitekCommand?,
    onSelect : (MagitekCommand) -> Unit,
    modifier : Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns             = GridCells.Fixed(8),
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement  = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding      = PaddingValues(2.dp),
    ) {
        items(commands.size) { i ->
            MagitekButton(
                command    = commands[i],
                isSelected = commands[i] == selected,
                onClick    = { onSelect(commands[i]) },
            )
        }
    }
}

@Composable
fun MagitekButton(
    command    : MagitekCommand,
    isSelected : Boolean,
    onClick    : () -> Unit,
) {
    val bgColor     = if (isSelected) GarlemaldColors.ImperialRedDark
                      else            GarlemaldColors.SurfaceVariant
    val borderColor = if (isSelected) GarlemaldColors.ImperialRedGlow
                      else            GarlemaldColors.Border
    val textColor   = if (isSelected) GarlemaldColors.OnImperialRed
                      else            GarlemaldColors.MetalLight

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(bgColor)
            .border(1.dp, borderColor)
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Petit relief — ligne claire en haut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    if (isSelected) GarlemaldColors.ImperialRed
                    else GarlemaldColors.MetalDark
                ),
        )
        Text(
            text      = command.label,
            style     = MaterialTheme.typography.labelLarge.copy(
                color = textColor,
                fontSize = 8.sp,
            ),
            textAlign = TextAlign.Center,
            maxLines  = 1,
        )
    }
}

// ── Barre de statut ───────────────────────────────────────────────────────────

@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .border(1.dp, GarlemaldColors.Border)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf(
            "FREQ: --",
            "CONN: ---",
            "PROXIM: ---",
            "MODE: CTL",
        ).forEach { label ->
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 8.sp),
            )
        }
    }
}
