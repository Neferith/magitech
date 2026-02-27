import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import org.angelus.magitek.GarlemaldColors
import org.angelus.magitek.GarlemaldTheme
import org.angelus.magitek.rememberFeedbackController

// ── Modèle ────────────────────────────────────────────────────────────────────
/*
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
}*/

// commonMain/kotlin/org/angelus/magitek/ui/MagitekRemoteScreen.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.angelus.magitek.CommandPickerDialog
import org.angelus.magitek.MacroEditorDialog
import org.angelus.magitek.displayDescription
import org.angelus.magitek.model.ButtonAssignment
import org.angelus.magitek.model.ButtonConfig
import org.angelus.magitek.model.buildDefaultButtonConfigs
import org.angelus.magitek.model.toDisplayBin64
import org.angelus.magitek.model.toHex16
import org.angelus.magitek.model.displayLabel
import org.angelus.magitek.repository.rememberButtonRepository

// ── État d'un bouton ──────────────────────────────────────────────────────────

data class ButtonState(
    val index      : Int,
    val config     : ButtonConfig?,   // null = non assigné
)

// ── Écran principal ───────────────────────────────────────────────────────────

@Composable
fun MagitekRemoteScreen() {

    val repository = rememberButtonRepository()
    val feedback   = rememberFeedbackController()
    val scope      = rememberCoroutineScope()

    var runningMacroIndex by remember { mutableStateOf<Int?>(null) }
    var runningMacroJob   by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Chargement de la configuration persistée
    var buttonConfigs by remember {
        mutableStateOf(
            buildDefaultButtonConfigs() + repository.loadConfigs()
            // Les configs sauvegardées écrasent les defaults pour les boutons configurés
        )
    }
    // Commande / log affiché sur l'écran terminal
    var screenLog by remember { mutableStateOf<List<String>>(listOf("> ATTENTE COMMANDE...", "> _")) }

    // Dialogs
    var editingButtonIndex by remember { mutableStateOf<Int?>(null) }   // appui long → assignation
    var showAssignTypeFor  by remember { mutableStateOf<Int?>(null) }   // choix : commande ou macro
    var showCommandPicker  by remember { mutableStateOf<Int?>(null) }
    var showMacroPicker    by remember { mutableStateOf<Int?>(null) }

    // Dispose feedback
    DisposableEffect(Unit) { onDispose { feedback.release() } }

    // ── Gestion d'une pression simple (exécution) ─────────────────────────────
    /*fun executeButton(index: Int) {
        val config = buttonConfigs[index] ?: return
        feedback.triggerCommandFeedback()

        when (val assignment = config.assignment) {
            is ButtonAssignment.SingleCommand -> {
                val bits = assignment.command.encode64()
                screenLog = listOf(
                    "> CMD: ${assignment.displayLabel(config.customLabel)}", // Bonne valeur ?
                    "> HEX: 0x${bits.toHex16()}",
                    "> BIN: ${bits.toDisplayBin64()}",
                    "> ${assignment.command.displayDescription()}",
                )
            }
            is ButtonAssignment.Macro -> {
                scope.launch {
                    screenLog = listOf("> MACRO: ${assignment.name}", "> EXÉCUTION...")
                    do {
                        assignment.steps.forEachIndexed { i, step ->
                            val bits = step.command.encode64()
                            screenLog = listOf(
                                "> MACRO [${i + 1}/${assignment.steps.size}]: ${assignment.name}",
                                "> CMD: ${step.command.shortLabel()}",
                                "> HEX: 0x${bits.toHex16()}",
                            )
                            feedback.triggerCommandFeedback()
                            delay(step.delayAfterMs)
                        }
                    } while (assignment.loop)
                    if (!assignment.loop) screenLog = listOf("> MACRO: ${assignment.name}", "> TERMINÉE.")
                }
            }
        }
    }*/
    fun executeButton(index: Int) {
        val config = buttonConfigs[index]

        if (config == null) {
            screenLog = listOf(
                "> BTN-${index.toString().padStart(2, '0')}: NON ASSIGNÉ",
                "> APPUI LONG POUR CONFIGURER",
            )
            return
        }

        when (val assignment = config.assignment) {
            is ButtonAssignment.SingleCommand -> {
                feedback.triggerCommandFeedback()
                val bits = assignment.command.encode64()
                screenLog = listOf(
                    "> CMD: ${assignment/*.command*/.displayLabel(config.customLabel)}",
                    "> HEX: 0x${bits.toHex16()}",
                    "> BIN: ${bits.toDisplayBin64()}",
                    "> ${assignment.command.displayDescription()}",
                )
            }

            is ButtonAssignment.Macro -> {
                // Si cette macro est déjà en cours → on l'arrête
                if (runningMacroIndex == index) {
                    runningMacroJob?.cancel()
                    runningMacroJob   = null
                    runningMacroIndex = null
                    screenLog = listOf(
                        "> MACRO: ${assignment.name}",
                        "> ARRÊTÉE.",
                    )
                    return
                }

                // Si une autre macro tourne → on l'arrête d'abord
                runningMacroJob?.cancel()

                runningMacroIndex = index
                runningMacroJob = scope.launch {
                    screenLog = listOf("> MACRO: ${assignment.name}", "> EXÉCUTION...")
                    try {
                        do {
                            assignment.steps.forEachIndexed { i, step ->
                                val bits = step.command.encode64()
                                screenLog = listOf(
                                    "> MACRO [${i + 1}/${assignment.steps.size}]: ${assignment.name}",
                                    "> CMD: ${step.command.shortLabel()}",
                                    "> HEX: 0x${bits.toHex16()}",
                                )
                                feedback.triggerCommandFeedback()
                                kotlinx.coroutines.delay(step.delayAfterMs)
                            }
                        } while (assignment.loop)

                        if (!assignment.loop) screenLog = listOf("> MACRO: ${assignment.name}", "> TERMINÉE.")
                    } catch (_: kotlinx.coroutines.CancellationException) {
                        // Annulation propre — screenLog déjà mis à jour
                    } finally {
                        if (runningMacroIndex == index) {
                            runningMacroIndex = null
                            runningMacroJob   = null
                        }
                    }
                }
            }
        }
    }

    GarlemaldTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = GarlemaldColors.Background) {
            Column(
                modifier            = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ImperialHeader()
                CommandScreen(lines = screenLog)
                ButtonGrid(
                    buttonConfigs    = buttonConfigs,
                    runningMacroIndex = runningMacroIndex,
                    onTap            = { index -> executeButton(index) },
                    onLongPress      = { index -> editingButtonIndex = index; showAssignTypeFor = index },
                    modifier         = Modifier.weight(1f),
                )
                StatusBar()
            }
        }
    }

    // ── Dialog : choix du type d'assignation ──────────────────────────────────
    showAssignTypeFor?.let { idx ->
        AssignTypeDialog(
            currentConfig = buttonConfigs[idx],
            onSingleCommand = { showAssignTypeFor = null; showCommandPicker = idx },
            onMacro         = { showAssignTypeFor = null; showMacroPicker   = idx },
            onClear         = {
                showAssignTypeFor = null
                repository.deleteConfig(idx)
                buttonConfigs = buttonConfigs.toMutableMap().also { it.remove(idx) }
            },
            onDismiss       = { showAssignTypeFor = null },
        )
    }

    // ── Dialog : picker commande simple ───────────────────────────────────────
    showCommandPicker?.let { idx ->
        val existing = (buttonConfigs[idx]?.assignment as? ButtonAssignment.SingleCommand)?.command
        CommandPickerDialog(
            initial   = existing,
            onConfirm = { spec ->
                val config = ButtonConfig(
                    buttonIndex  = idx,
                    assignment   = ButtonAssignment.SingleCommand(spec),
                    customLabel  = buttonConfigs[idx]?.customLabel,
                )
                repository.saveConfig(config)
                buttonConfigs = buttonConfigs.toMutableMap().also { it[idx] = config }
                showCommandPicker = null
            },
            onDismiss = { showCommandPicker = null },
        )
    }

    // ── Dialog : éditeur macro ────────────────────────────────────────────────
    showMacroPicker?.let { idx ->
        val existing = buttonConfigs[idx]?.assignment as? ButtonAssignment.Macro
        MacroEditorDialog(
            initial   = existing,
            onConfirm = { macro ->
                val config = ButtonConfig(
                    buttonIndex = idx,
                    assignment  = macro,
                    customLabel = macro.name,
                )
                repository.saveConfig(config)
                buttonConfigs = buttonConfigs.toMutableMap().also { it[idx] = config }
                showMacroPicker = null
            },
            onDismiss = { showMacroPicker = null },
        )
    }
}

// ── Dialog : choix du type d'assignation ─────────────────────────────────────

@Composable
fun AssignTypeDialog(
    currentConfig  : ButtonConfig?,
    onSingleCommand: () -> Unit,
    onMacro        : () -> Unit,
    onClear        : () -> Unit,
    onDismiss      : () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = GarlemaldColors.Surface,
        titleContentColor = GarlemaldColors.ImperialRed,
        title = {
            Text("ASSIGNER BOUTON", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssignOption("COMMANDE SIMPLE", GarlemaldColors.ScreenGreen, onSingleCommand)
                AssignOption("MACRO",           GarlemaldColors.MagitekBlue, onMacro)
                if (currentConfig != null) {
                    AssignOption("EFFACER", GarlemaldColors.ImperialRed, onClear)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Text(
                text     = "ANNULER",
                style    = MaterialTheme.typography.labelLarge.copy(
                    color = GarlemaldColors.MetalLight, fontSize = 10.sp,
                ),
                modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp),
            )
        },
    )
}

@Composable
private fun AssignOption(label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge.copy(color = color, fontSize = 11.sp),
        )
    }
}

// ── Grille de 64 boutons ──────────────────────────────────────────────────────

@Composable
fun ButtonGrid(
    buttonConfigs     : Map<Int, ButtonConfig>,
    runningMacroIndex : Int?,
    onTap             : (Int) -> Unit,
    onLongPress       : (Int) -> Unit,
    modifier          : Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(8),
        modifier              = modifier.fillMaxWidth(),
        verticalArrangement   = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding        = PaddingValues(2.dp),
    ) {
        items(64) { index ->
            MagitekButton(
                index       = index,
                config      = buttonConfigs[index],
                isRunning   = runningMacroIndex == index,
                onTap       = { onTap(index) },
                onLongPress = { onLongPress(index) },
            )
        }
    }
}
// ── MagitekButton — ajouter isRunning ────────────────────────────────────────

@Composable
fun MagitekButton(
    index      : Int,
    config     : ButtonConfig?,
    isRunning  : Boolean,
    onTap      : () -> Unit,
    onLongPress: () -> Unit,
) {
    // Animation de pulsation pour la macro en cours
    val infiniteTransition = rememberInfiniteTransition(label = "running_$index")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_$index",
    )

    val isMacro     = config?.assignment is ButtonAssignment.Macro
    val isAssigned  = config != null
    val bgColor     = when {
        isRunning  -> GarlemaldColors.MagitekBlueDim
        isMacro    -> GarlemaldColors.MagitekBlueDim.copy(alpha = 0.5f)
        isAssigned -> GarlemaldColors.SurfaceVariant
        else       -> GarlemaldColors.Background
    }
    val borderColor = when {
        isRunning  -> GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha)
        isMacro    -> GarlemaldColors.MagitekBlue
        isAssigned -> GarlemaldColors.Border
        else       -> GarlemaldColors.MetalDark.copy(alpha = 0.4f)
    }
    val label = config?.assignment?.displayLabel(config.customLabel)
        ?: index.toString().padStart(2, '0')

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(bgColor)
            .border(1.dp, borderColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onTap() },
                    onLongPress = { onLongPress() },
                )
            }
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(borderColor.copy(alpha = 0.5f)),
        )
        // Indicateur ■ STOP si macro en cours
        if (isRunning) {
            Text(
                text  = "■",
                style = MaterialTheme.typography.labelLarge.copy(
                    color    = GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha),
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelLarge.copy(
                    color    = if (isAssigned) GarlemaldColors.OnSurface else GarlemaldColors.MetalDark,
                    fontSize = 7.sp,
                ),
                textAlign = TextAlign.Center,
                maxLines  = 1,
                overflow  = TextOverflow.Clip,
            )
        }
    }
}

// ── Écran terminal ────────────────────────────────────────────────────────────

@Composable
fun CommandScreen(lines: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(GarlemaldColors.ScreenBackground)
            .border(2.dp, GarlemaldColors.ScreenGreenDim)
            .drawBehind { drawScanlines(this) }
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            lines.forEachIndexed { i, line ->
                Text(
                    text  = line,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (i == 0) 11.sp else 9.sp,
                        color    = if (i == 0) GarlemaldColors.ScreenGreen else GarlemaldColors.ScreenGreenDim,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
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
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "diode_alpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(GarlemaldColors.DiodRed.copy(alpha = alpha), RoundedCornerShape(50))
            .border(1.dp, GarlemaldColors.DiodRed.copy(alpha = 0.5f), RoundedCornerShape(50)),
    )
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
        listOf("FREQ: --", "CONN: ---", "PROXIM: ---", "MODE: CTL").forEach { label ->
            Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 8.sp))
        }
    }
}

// ── Scanlines CRT ─────────────────────────────────────────────────────────────

fun drawScanlines(scope: DrawScope) {
    val lineColor = Color(0x0A00FF88)
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(lineColor, Offset(0f, y), Offset(scope.size.width, y), strokeWidth = 1.5f)
        y += 4f
    }
}
