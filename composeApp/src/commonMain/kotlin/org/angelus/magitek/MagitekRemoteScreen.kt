package org.angelus.magitek

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.angelus.magitek.audio.rememberStaticHumPlayer
import org.angelus.magitek.model.ActivationFrequency
import org.angelus.magitek.model.ButtonAssignment
import org.angelus.magitek.model.ButtonConfig
import org.angelus.magitek.model.ButtonLabelEncoder
import org.angelus.magitek.model.CommandSpec
import org.angelus.magitek.model.ContradictionDetector
import org.angelus.magitek.model.EditModeController
import org.angelus.magitek.model.HiddenMessageEngine
import org.angelus.magitek.model.HiddenState
import org.angelus.magitek.model.OverflowState
import org.angelus.magitek.model.ResonanceLevel
import org.angelus.magitek.model.buildActivationFrequencies
import org.angelus.magitek.model.buildContradictionRules
import org.angelus.magitek.model.buildDefaultButtonConfigs
import org.angelus.magitek.model.buildDefaultSecrets
import org.angelus.magitek.model.buildEditModeController
import org.angelus.magitek.model.buildLocations
import org.angelus.magitek.model.buildOverflowState
import org.angelus.magitek.model.detectWithLevel
import org.angelus.magitek.model.toDisplayBin64
import org.angelus.magitek.model.toHex16
import org.angelus.magitek.model.displayLabel
import org.angelus.magitek.model.findById
import org.angelus.magitek.repository.rememberButtonRepository
import org.angelus.magitek.settings.rememberMagitekSettings
import org.angelus.magitek.settings.saveFrequency
import org.angelus.magitek.ui.FrequencyDial
import org.angelus.magitek.ui.GlitchEngine
import org.angelus.magitek.ui.LifecycleEffect
import org.angelus.magitek.ui.rememberGlitchEngine
import kotlin.random.Random

// ── État d'un bouton ──────────────────────────────────────────────────────────

data class ButtonState(
    val index: Int,
    val config: ButtonConfig?,   // null = non assigné
)

// ── Écran principal ───────────────────────────────────────────────────────────

@Composable
fun MagitekRemoteScreen(
    editModeController: EditModeController,
    isEditMode: Boolean,
    screenLog: List<String>,
    onScreenLogChange: (List<String>) -> Unit,
    globalFrequency: Long,
    onGlobalFrequencyChange: (Long) -> Unit,
) {

    var overflowState by remember { mutableStateOf(OverflowState()) }

    // Override — ignore les logs normaux pendant l'overflow
    val safeLogChange: (List<String>) -> Unit = { lines ->
        if (!overflowState.isActive) onScreenLogChange(lines)
    }

    val contradictionDetector = remember {
        ContradictionDetector(
            rules = buildContradictionRules(),
            intervalMs = 100L,
            durationMs = 20_000L,
            onOverflow = { rule ->
                overflowState = buildOverflowState(rule.overflowMsg)
            },
        )
    }


    val repository = rememberButtonRepository()
    val feedback = rememberFeedbackController()
    val scope = rememberCoroutineScope()

    val staticHum = rememberStaticHumPlayer()

    val glitchEngine = rememberGlitchEngine()
    val appSettings = rememberMagitekSettings()
    val locations = remember { buildLocations() }
    val currentLocation by remember(appSettings.locationId) {
        derivedStateOf { locations.findById(appSettings.locationId) }
    }

    val activationFrequencies by remember(currentLocation) {
        derivedStateOf { currentLocation.frequencies }
    }
    var resonanceLevel by remember { mutableStateOf<ResonanceLevel?>(null) }

    var isDraggingDial by remember { mutableStateOf(false) }


// C'est tout. Le son démarre automatiquement au lancement via LaunchedEffect
// et s'arrête proprement au DisposableEffect.

// Optionnel — si tu veux ajuster le volume dynamiquement :
    staticHum.volume = 0.009f  // encore plus discret
// staticHum.volume = 0.08f  // un peu plus présent

    var runningMacroIndex by remember { mutableStateOf<Int?>(null) }
    var runningMacroJob by remember { mutableStateOf<Job?>(null) }

    val hiddenEngine = remember { HiddenMessageEngine(buildDefaultSecrets()) }
    var hiddenState by remember { mutableStateOf<HiddenState>(HiddenState.Idle) }

    // Chargement de la configuration persistée
    var buttonConfigs by remember {
        mutableStateOf(
            buildDefaultButtonConfigs() + repository.loadConfigs()
            // Les configs sauvegardées écrasent les defaults pour les boutons configurés
        )
    }
    // Commande / log affiché sur l'écran terminal
    //  var screenLog by remember { mutableStateOf<List<String>>(listOf("> ATTENTE COMMANDE...", "> _")) }

    // Dialogs
    var editingButtonIndex by remember { mutableStateOf<Int?>(null) }   // appui long → assignation
    var showAssignTypeFor by remember { mutableStateOf<Int?>(null) }   // choix : commande ou macro
    var showCommandPicker by remember { mutableStateOf<Int?>(null) }
    var showMacroPicker by remember { mutableStateOf<Int?>(null) }


    val globalFrequencyRef = rememberUpdatedState(globalFrequency)

    //var globalFrequency by remember { mutableStateOf(0L) }

    //val editModeController = remember { buildEditModeController() }
    // val isEditMode by remember { derivedStateOf { editModeController.isUnlocked } }

    // Dispose feedback
    DisposableEffect(Unit) { onDispose { feedback.release() } }

    /*LaunchedEffect(Unit) {
        while (true) {
            // Attente aléatoire entre 4 et 20 secondes
            delay(Random.nextLong(4_000L, 8_000L))
            feedback.triggerRandomVibration()
        }
    }*/

    LaunchedEffect(appSettings.humVolume) {
        if (!isDraggingDial && resonanceLevel != null) {
            val target = appSettings.humVolume / 100f * 0.3f  // 0..0.3
            staticHum.volume = target
        }
    }

    LaunchedEffect(appSettings.randomVibration) {
        if (appSettings.randomVibration) {
            while (true) {
                delay(Random.nextLong(4_000L, 20_000L))
                glitchEngine.triggerGlitch(this, screenLog)
                feedback.triggerRandomVibration()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            // Intervalle aléatoire entre 6 et 30 secondes
            delay(Random.nextLong(6_000L, 15_000L))
            feedback.triggerGlitchSound()
        }
    }

    // Dans MagitekRemoteScreen, après les autres LaunchedEffect :

// Détection initiale au démarrage avec la fréquence restaurée
    LaunchedEffect(Unit) {
        val detected = activationFrequencies.detectWithLevel(
            frequency = globalFrequency,
            currentX  = appSettings.currentX,
            currentY  = appSettings.currentY,
        )
        resonanceLevel = detected
    }

    // Recalcul si la position change
    LaunchedEffect(appSettings.currentX, appSettings.currentY) {
        val detected = activationFrequencies.detectWithLevel(
            frequency = globalFrequency,
            currentX  = appSettings.currentX,
            currentY  = appSettings.currentY,
        )
        resonanceLevel = detected
    }

    LaunchedEffect(isDraggingDial, resonanceLevel) {
       /* if (isDraggingDial) {
            // Monte pendant le drag
            while (staticHum.volume < 0.25f) {
                staticHum.volume = (staticHum.volume + 0.02f).coerceAtMost(0.25f)
                delay(16L)
            }
        } else {
            delay(400L)
            // Reste fort si résonance active, redescend sinon
            val target = if (activeFrequency != null) 0.25f else 0.04f
            if (target > staticHum.volume) {
                while (staticHum.volume < target) {
                    staticHum.volume = (staticHum.volume + 0.02f).coerceAtMost(target)
                    delay(16L)
                }
            } else {
                while (staticHum.volume > target) {
                    staticHum.volume = (staticHum.volume - 0.015f).coerceAtLeast(target)
                    delay(16L)
                }
            }
        }*/
        if (isDraggingDial) {
            while (staticHum.volume < 0.25f) {
                staticHum.volume = (staticHum.volume + 0.02f).coerceAtMost(0.25f)
                delay(16L)
            }
        } else {
            delay(400L)
            val target = resonanceLevel?.let { 0.04f + it.level * 0.21f } ?: 0.04f
            if (target > staticHum.volume) {
                while (staticHum.volume < target) {
                    staticHum.volume = (staticHum.volume + 0.02f).coerceAtMost(target)
                    delay(16L)
                }
            } else {
                while (staticHum.volume > target) {
                    staticHum.volume = (staticHum.volume - 0.015f).coerceAtLeast(target)
                    delay(16L)
                }
            }
        }
    }

    val dissonanceLabels = remember {
        listOf(
            "!! OVERFLOW !!",
            "!! DISSONANCE !!",
            "!! CONVULSION !!",
            "!! EXTRACTION URGENCE !!",
        )
    }

    LaunchedEffect(overflowState.isActive) {
        if (!overflowState.isActive) return@LaunchedEffect
        while (true) {
            // Glitch visuel intense — intervalles très courts
            delay(Random.nextLong(300L, 800L))
            glitchEngine.triggerGlitch(this, screenLog)
            feedback.triggerGlitchSound()

            // Affichage alterné : message encodé ↔ corruption totale
            val words    = overflowState.encodedWords
            val allCodes = words.flatten()
           onScreenLogChange(
               listOf(
                "> ${dissonanceLabels.random()}",
                "> ${allCodes.shuffled().take(6).joinToString(" ")}",
                "> ERR:${Random.nextInt(0xFFFF).toString(16).uppercase().padStart(4, '0')}",
                "> ${allCodes.random()} ${allCodes.random()} ${allCodes.random()}",
            )
           )
        }
    }


    // ── Gestion d'une pression simple (exécution) ─────────────────────────────
    fun executeButton(index: Int) {

        val wasEditMode = isEditMode
        val modeChanged = editModeController.onButtonPressed(index, scope, isEditMode)
        if (modeChanged) {
            safeLogChange(
                if (!wasEditMode) listOf(
                    "> MODE ÉDITION ACTIVÉ",
                    "> APPUI LONG POUR CONFIGURER",
                ) else listOf(
                    "> MODE ÉDITION DÉSACTIVÉ",
                    "> _",
                )
            )
            return  // on ne déclenche pas de commande sur la séquence de déverrouillage
        }

        // ← ICI, en tout premier, avant le check config == null
        hiddenState = hiddenEngine.onButtonPressed(index)


        val config = buttonConfigs[index]

        if (config == null) {
            safeLogChange(
                listOf(
                    "> BTN-${index.toString().padStart(2, '0')}: NON ASSIGNÉ",
                    "> APPUI LONG POUR CONFIGURER",
                )
            )
            return
        }

        when (val assignment = config.assignment) {
            is ButtonAssignment.SingleCommand -> {
                feedback.triggerCommandFeedback()
                //  val bits = assignment.command.encode64()
                val bits = assignment.command.encode64WithFreq(globalFrequency)
                safeLogChange(
                    listOfNotNull(
                        "> CMD: ${/*assignment/*.command*/.displayLabel(config.customLabel)*/ButtonLabelEncoder.encode(
                            config.buttonIndex
                        )
                        }",
                        "> HEX: 0x${bits.toHex16()}",
                        "> BIN: ${bits.toDisplayBin64()}",
                        //"> ${assignment.command.displayDescription()}",
                        if (isEditMode) "> ${assignment.command.displayDescription()}" else null,
                    )
                )
                contradictionDetector.onCommandExecuted(assignment.command)
            }

            is ButtonAssignment.Macro -> {
                // Si cette macro est déjà en cours → on l'arrête
                if (runningMacroIndex == index) {
                    runningMacroJob?.cancel()
                    runningMacroJob = null
                    runningMacroIndex = null
                    safeLogChange(
                        listOf(
                            "> MACRO: ${assignment.name}",
                            "> ARRÊTÉE.",
                        )
                    )
                    overflowState = OverflowState()
                    contradictionDetector.reset()
                    return
                }

                // Si une autre macro tourne → on l'arrête d'abord
                runningMacroJob?.cancel()

                runningMacroIndex = index
                runningMacroJob = scope.launch {
                    safeLogChange(listOf("> MACRO: ${assignment.name}", "> EXÉCUTION..."))
                    try {
                        do {
                            assignment.steps.forEachIndexed { i, step ->
                                //val bits = step.command.encode64()
                                val bits = step.command.encode64WithFreq(globalFrequency)
                                /*screenLog = listOf(
                                    "> MACRO [${i + 1}/${assignment.steps.size}]: ${assignment.name}",
                                    "> CMD: ${step.command.shortLabel()}",
                                    "> HEX: 0x${bits.toHex16()}",
                                )*/
                                safeLogChange(
                                    listOf(
                                        "> CMD: ${
                                            config.customLabel ?: "BTN-${
                                                index.toString().padStart(2, '0')
                                            }"
                                        }",
                                        "> HEX: 0x${bits.toHex16()}",
                                        "> BIN: ${bits.toDisplayBin64()}",
                                        // description supprimée — uniquement en mode édition
                                    ) + if (isEditMode) listOf("> ${step.command.displayDescription()}") else emptyList()
                                )
                                feedback.triggerCommandFeedback()
                                contradictionDetector.onCommandExecuted(step.command)
                                delay(step.delayAfterMs)
                            }
                        } while (assignment.loop)

                        if (!assignment.loop) {
                            safeLogChange(listOf("> MACRO: ${assignment.name}", "> TERMINÉE."))
                        }
                    } catch (_: CancellationException) {
                        // Annulation propre — screenLog déjà mis à jour
                    } finally {
                        if (runningMacroIndex == index) {
                            runningMacroIndex = null
                            runningMacroJob = null
                        }
                    }
                }
            }
        }
    }

    LifecycleEffect(
        onPause = { staticHum.stop() },
        onResume = { staticHum.start(scope) },
    )

    GarlemaldTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = GarlemaldColors.Background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ImperialHeader(
                    resonanceLevel  = resonanceLevel,
                    locationName    = currentLocation.name.takeIf { it != "Aucun" },
                    glitchEngine = glitchEngine,
                )
                CommandScreen(
                    lines = screenLog,
                    hiddenState = hiddenState,
                    resonanceLevel = resonanceLevel,
                    glitchEngine = glitchEngine,
                    isEditMode = isEditMode,
                )
                ButtonGrid(
                    buttonConfigs = buttonConfigs,
                    runningMacroIndex = runningMacroIndex,
                    isEditMode = isEditMode,
                    glitchEngine = glitchEngine,
                    onTap = { index -> executeButton(index) },
                    onLongPress = { index ->
                        editingButtonIndex = index;
                        if (isEditMode) showAssignTypeFor = index
                    },
                    modifier = Modifier.weight(1f),
                )
                FrequencyDial(
                    frequency = globalFrequency,
                    onChange = { freq ->
                       /* globalFrequency = freq
                        val detected = activationFrequencies.detect(freq)
                        if (detected != activeFrequency) {
                            activeFrequency = detected
                            if (detected != null) {
                                feedback.triggerActivationSound()
                            }
                        }*/
                       // globalFrequency = freq
                        onGlobalFrequencyChange(freq)
                        val detected = activationFrequencies.detectWithLevel(
                            frequency = freq,
                            currentX  = appSettings.currentX,
                            currentY  = appSettings.currentY,
                        )
                        if (detected?.frequency != resonanceLevel?.frequency) {
                            resonanceLevel = detected
                            if (detected != null) feedback.triggerActivationSound()
                        } else {
                            // Même fréquence — mise à jour du niveau seulement
                            resonanceLevel = detected
                        }

                    },
                    onDragStart = { isDraggingDial = true },
                    onDragEnd = {
                        isDraggingDial = false
                        saveFrequency(globalFrequencyRef.value)
                                },
                    onDetent = { feedback.triggerDialClick() },
                )
                StatusBar(frequency = globalFrequency)
            }
        }
    }

    // ── Dialog : choix du type d'assignation ──────────────────────────────────
    showAssignTypeFor?.let { idx ->
        AssignTypeDialog(
            currentConfig = buttonConfigs[idx],
            onSingleCommand = { showAssignTypeFor = null; showCommandPicker = idx },
            onMacro = { showAssignTypeFor = null; showMacroPicker = idx },
            onClear = {
                showAssignTypeFor = null
                repository.deleteConfig(idx)
                buttonConfigs = buttonConfigs.toMutableMap().also { it.remove(idx) }
            },
            onDismiss = { showAssignTypeFor = null },
        )
    }

    // ── Dialog : picker commande simple ───────────────────────────────────────
    showCommandPicker?.let { idx ->
        val existing = (buttonConfigs[idx]?.assignment as? ButtonAssignment.SingleCommand)?.command
        CommandPickerDialog(
            initial = existing,
            onConfirm = { spec ->
                val config = ButtonConfig(
                    buttonIndex = idx,
                    assignment = ButtonAssignment.SingleCommand(spec),
                    customLabel = buttonConfigs[idx]?.customLabel,
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
            initial = existing,
            onConfirm = { macro ->
                val config = ButtonConfig(
                    buttonIndex = idx,
                    assignment = macro,
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
    currentConfig: ButtonConfig?,
    onSingleCommand: () -> Unit,
    onMacro: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GarlemaldColors.Surface,
        titleContentColor = GarlemaldColors.ImperialRed,
        title = {
            Text("ASSIGNER BOUTON", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssignOption("COMMANDE SIMPLE", GarlemaldColors.ScreenGreen, onSingleCommand)
                AssignOption("MACRO", GarlemaldColors.MagitekBlue, onMacro)
                if (currentConfig != null) {
                    AssignOption("EFFACER", GarlemaldColors.ImperialRed, onClear)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Text(
                text = "ANNULER",
                style = MaterialTheme.typography.labelLarge.copy(
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
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(color = color, fontSize = 11.sp),
        )
    }
}

// ── Grille de 64 boutons ──────────────────────────────────────────────────────

@Composable
fun ButtonGrid(
    buttonConfigs: Map<Int, ButtonConfig>,
    runningMacroIndex: Int?,
    isEditMode: Boolean,
    glitchEngine: GlitchEngine,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier
            .fillMaxWidth()
            .offset(x = glitchEngine.gridShake.dp, y = 0.dp),  // ← tremblement
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(2.dp),
    ) {
        items(64) { index ->
            MagitekButton(
                index = index,
                config = buttonConfigs[index],
                isRunning = runningMacroIndex == index,
                isEditMode = isEditMode,
                onTap = { onTap(index) },
                onLongPress = { onLongPress(index) },
            )
        }
    }
}
// ── MagitekButton — ajouter isRunning ────────────────────────────────────────

// Dans MagitekRemoteScreen.kt — remplacer MagitekButton par cette version

@Composable
fun MagitekButton(
    index: Int,
    config: ButtonConfig?,
    isRunning: Boolean,
    isEditMode: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "running_$index")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_$index",
    )

    val isMacro = config?.assignment is ButtonAssignment.Macro
    val isAssigned = config != null

    val bgColor = when {
        isPressed -> GarlemaldColors.ImperialRedDark
        isRunning -> GarlemaldColors.MagitekBlueDim
        isMacro -> GarlemaldColors.MagitekBlueDim.copy(alpha = 0.5f)
        isAssigned -> GarlemaldColors.SurfaceVariant
        isEditMode -> GarlemaldColors.ScreenBackground   // non assigné en mode édition
        else -> GarlemaldColors.Background
    }
    val borderColor = when {
        isPressed -> GarlemaldColors.ImperialRedGlow
        isRunning -> GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha)
        isMacro -> GarlemaldColors.MagitekBlue
        isAssigned -> GarlemaldColors.Border
        isEditMode -> GarlemaldColors.ScreenGreenDim.copy(alpha = 0.4f)  // contour vert discret
        else -> GarlemaldColors.MetalDark.copy(alpha = 0.4f)
    }
    val textColor = when {
        isPressed -> GarlemaldColors.OnImperialRed
        isAssigned -> GarlemaldColors.OnSurface
        else -> GarlemaldColors.MetalDark
    }

    val label = ButtonLabelEncoder.encode(index)/*config?.assignment?.displayLabel(config.customLabel)
        ?: index.toString().padStart(2, '0')*/

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(bgColor)
            .border(1.dp, borderColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // Émettre immédiatement le press → visuel rouge instantané
                        val press = PressInteraction.Press(offset)
                        scope.launch { interactionSource.emit(press) }
                        val released = tryAwaitRelease()
                        scope.launch {
                            if (released) interactionSource.emit(PressInteraction.Release(press))
                            else interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    },
                    onTap = { onTap() },
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
        if (isRunning) {
            Text(
                text = "■",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha),
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = textColor,
                    fontSize = 7.sp,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

// ── Écran terminal ────────────────────────────────────────────────────────────

@Composable
fun CommandScreen(
    lines: List<String>,
    hiddenState: HiddenState,
    isEditMode: Boolean,
    resonanceLevel : ResonanceLevel? = null,
    glitchEngine: GlitchEngine,
) {
    val displayLines = glitchEngine.corruptedLines ?: lines
    val shift = glitchEngine.scanlineShift

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(GarlemaldColors.ScreenBackground)
            .border(
                width = 2.dp,
                color = when {
                    resonanceLevel?.isComplete == true -> GarlemaldColors.ImperialRed
                    resonanceLevel != null             -> GarlemaldColors.ImperialRed.copy(alpha = resonanceLevel.level)
                    else                               -> GarlemaldColors.ScreenGreenDim
                },
            )
            .drawBehind {
                // Scanlines avec shift
                drawScanlines(this, offsetX = shift)
                // Flash d'inversion
                if (glitchEngine.flashIntensity > 0f) {
                    drawRect(
                        color = GarlemaldColors.ScreenGreen.copy(alpha = glitchEngine.flashIntensity),
                        size = size,
                        blendMode = BlendMode.Difference,
                    )
                }
            }
            .padding(10.dp),
    ) {
        Column(
            modifier = Modifier.offset(x = shift.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            displayLines.forEachIndexed { i, line ->
                val displayLine =
                    if (i == 2 && line.startsWith("> BIN:") && glitchEngine.corruptedLines == null) {
                        injectHiddenBits(line, hiddenState)
                    } else line

                Text(
                    text = displayLine,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (i == 0) 11.sp else 9.sp,
                        color = if (glitchEngine.corruptedLines != null)
                            GarlemaldColors.ScreenGreen.copy(alpha = 0.7f)
                        else lineColor(i, hiddenState),
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
            if (isEditMode && hiddenState is HiddenState.Complete) {
                Text(
                    text = "> !! ${hiddenState.fullMessage} !!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 11.sp,
                        color = GarlemaldColors.ImperialRedGlow,
                        letterSpacing = 3.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
        // Indicateur résonance en bas à droite
        if (resonanceLevel != null) {
            Text(
                text     = if (resonanceLevel.isComplete) "◆ SYNC" else "◇ ${resonanceLevel.percent}%",
                style    = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 9.sp,
                    color    = GarlemaldColors.ImperialRed.copy(
                        alpha = 0.5f + resonanceLevel.level * 0.5f
                    ),
                ),
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// drawScanlines mis à jour avec offsetX
// ─────────────────────────────────────────────────────────────────────────────

fun drawScanlines(scope: DrawScope, offsetX: Float = 0f) {
    val lineColor = Color(0x0A00FF88)
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(
            color = lineColor,
            start = Offset(offsetX, y),
            end = Offset(scope.size.width + offsetX, y),
            strokeWidth = 1.5f,
        )
        y += 4f
    }
}

// ── En-tête ───────────────────────────────────────────────────────────────────

@Composable
fun ImperialHeader(
    resonanceLevel  : ResonanceLevel? = null,
    locationName    : String? = null,
    glitchEngine    : GlitchEngine,
   // onSettingsClick : () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .border(
                width = 1.dp,
                color = when {
                    resonanceLevel?.isComplete == true -> GarlemaldColors.ImperialRed
                    resonanceLevel != null             -> GarlemaldColors.ImperialRed.copy(alpha = resonanceLevel.level * 0.8f)
                    else                               -> GarlemaldColors.ImperialRedDark
                },
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = resonanceLevel, label = "header_title") { lvl ->
                if (lvl != null) {
                    Text(
                        text  = ">> ${lvl.frequency.name} — ${lvl.label} (${lvl.percent}%) <<",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 10.sp,
                            color    = GarlemaldColors.ImperialRedGlow.copy(alpha = 0.5f + lvl.level * 0.5f),
                        ),
                    )
                } else {
                    Text(
                        text  = "UNITÉ MAGITEK — TÉLÉCOMMANDE v3.7",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 10.sp),
                    )
                }
            }
            if (locationName != null) {
                Text(
                    text  = "LOC: $locationName",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 7.sp,
                        color    = GarlemaldColors.ScreenGreenDim,
                    ),
                )
            }
        }

        // Diode — vitesse de pulse proportionnelle au niveau
        when {
            glitchEngine.diodeFlicker          -> DiodeGlitch()
            resonanceLevel?.isComplete == true -> DiodeFixed()
            resonanceLevel != null             -> DiodePulsing(speed = resonanceLevel.level)
            else                               -> DiodeIndicator()
        }

        Spacer(Modifier.width(8.dp))

     /*   Text(
            text     = "⚙",
            style    = MaterialTheme.typography.labelLarge.copy(
                color    = GarlemaldColors.MetalLight,
                fontSize = 16.sp,
            ),
            modifier = Modifier.clickable(onClick = onSettingsClick).padding(4.dp),
        )*/
    }
}

// Diode pulsante dont la vitesse augmente avec le niveau
@Composable
fun DiodePulsing(speed: Float) {
    val durationMs = (800f - speed * 600f).toInt().coerceIn(200, 800)  // 800ms lent → 200ms rapide
    val infiniteTransition = rememberInfiniteTransition(label = "diode_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.2f,
        targetValue   = speed.coerceAtLeast(0.4f),
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "diode_pulse_alpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                GarlemaldColors.DiodRed.copy(alpha = alpha),
                androidx.compose.foundation.shape.RoundedCornerShape(50),
            )
            .border(
                1.dp,
                GarlemaldColors.ImperialRedGlow.copy(alpha = alpha * 0.5f),
                androidx.compose.foundation.shape.RoundedCornerShape(50),
            ),
    )
}

/** Diode en mode glitch — scintille de façon erratique */
@Composable
fun DiodeGlitch() {
    val infiniteTransition = rememberInfiniteTransition(label = "diode_glitch")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "diode_glitch_alpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                GarlemaldColors.DiodRed.copy(alpha = alpha),
                androidx.compose.foundation.shape.RoundedCornerShape(50),
            )
            .border(
                1.dp,
                GarlemaldColors.MagitekBlue.copy(alpha = 1f - alpha),
                androidx.compose.foundation.shape.RoundedCornerShape(50),
            ),
    )
}

// Diode fixe (pas d'animation) pour fréquence verrouillée
@Composable
fun DiodeFixed() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                GarlemaldColors.DiodRed,
                androidx.compose.foundation.shape.RoundedCornerShape(50)
            )
            .border(
                1.dp,
                GarlemaldColors.ImperialRedGlow,
                androidx.compose.foundation.shape.RoundedCornerShape(50)
            ),
    )
}


@Composable
fun DiodeIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "diode")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
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
fun StatusBar(frequency: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .border(1.dp, GarlemaldColors.Border)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf(
            "FREQ: ${frequency.toString(16).uppercase().padStart(5, '0')}",
            "CONN: ---",
            "PROXIM: ---",
            "MODE: CTL",
        ).forEach { label ->
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


// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Remplace les 34 derniers bits de la ligne BIN par les bits du message caché.
 * Format attendu : "> BIN: [18 freq] | [4.4.4 cmd] | [34 réservés]"
 */
private fun injectHiddenBits(line: String, state: HiddenState): String {
    val bits34 = when (state) {
        is HiddenState.Revealing -> state.bits34
        is HiddenState.Complete -> state.bits34
        else -> return line
    }
    val sepIndex = line.lastIndexOf("| ")
    if (sepIndex < 0) return line
    return line.substring(0, sepIndex + 2) + bits34
}

/**
 * Couleur de la ligne selon l'état du message caché.
 */
@Composable
private fun lineColor(lineIndex: Int, state: HiddenState): Color = when {
    // La ligne BIN pulse en bleu magitek quand une séquence est en cours
    lineIndex == 2 && state is HiddenState.Revealing -> GarlemaldColors.MagitekBlue
    lineIndex == 2 && state is HiddenState.Complete -> GarlemaldColors.ImperialRedGlow
    lineIndex == 0 -> GarlemaldColors.ScreenGreen
    else -> GarlemaldColors.ScreenGreenDim
}

fun CommandSpec.encode64WithFreq(freq: Long): Long {
    val cmd12 = moduleCode.toLong(2).shl(8) or
            submoduleCode.toLong(2).shl(4) or
            actionCode.toLong(2)
    // La fréquence globale écrase le freqOverride du bouton si elle est non nulle
    val effectiveFreq = if (freq != 0L) freq else freqOverride
    return effectiveFreq.shl(46) or cmd12.shl(34)
}
