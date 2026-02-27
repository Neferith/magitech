package org.angelus.magitek// commonMain/kotlin/org/angelus/magitek/ui/MacroEditorDialog.kt

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import org.angelus.magitek.model.ButtonAssignment
import org.angelus.magitek.model.CommandSpec
import org.angelus.magitek.model.MacroStep

@Composable
fun MacroEditorDialog(
    initial  : ButtonAssignment.Macro? = null,
    onConfirm: (ButtonAssignment.Macro) -> Unit,
    onDismiss: () -> Unit,
) {
    var name  by remember { mutableStateOf(initial?.name ?: "MACRO") }
    var loop  by remember { mutableStateOf(initial?.loop ?: false) }
    var steps by remember { mutableStateOf<List<MacroStep>>(initial?.steps?.toMutableList() ?: mutableListOf()) }

    // Picker commande imbriqué
    var showPickerForIndex by remember { mutableStateOf<Int?>(null) }   // null = fermé, -1 = nouveau

    // Si le picker est ouvert
    showPickerForIndex?.let { idx ->
        CommandPickerDialog(
            initial   = if (idx >= 0) steps.getOrNull(idx)?.command else null,
            onConfirm = { spec ->
                steps = steps.toMutableList().also { list ->
                    if (idx == -1) list.add(MacroStep(command = spec))
                    else           list[idx] = list[idx].copy(command = spec)
                }
                showPickerForIndex = null
            },
            onDismiss = { showPickerForIndex = null },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color  = GarlemaldColors.Surface,
            border = BorderStroke(1.dp, GarlemaldColors.ImperialRed),
            modifier = Modifier.fillMaxWidth().heightIn(max = 580.dp),
        ) {
            Column(modifier = Modifier.padding(0.dp)) {

                // ── En-tête ──────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GarlemaldColors.SurfaceVariant)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text  = "ÉDITEUR DE MACRO",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    // Nom de la macro
                    GarlemaldTextField(
                        value    = name,
                        label    = "NOM",
                        onChange = { name = it.uppercase().take(16) },
                    )
                    // Switch loop
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text  = "BOUCLE INFINIE",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                        )
                        Switch(
                            checked         = loop,
                            onCheckedChange = { loop = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = GarlemaldColors.ImperialRed,
                                checkedTrackColor  = GarlemaldColors.ImperialRedDark,
                                uncheckedThumbColor = GarlemaldColors.MetalDark,
                                uncheckedTrackColor = GarlemaldColors.Border,
                            ),
                        )
                    }
                }

                HorizontalDivider(color = GarlemaldColors.Border)

                // ── Liste des étapes ─────────────────────────────────────────
                LazyColumn(
                    modifier        = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding  = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(steps) { idx, step ->
                        MacroStepRow(
                            index  = idx,
                            step   = step,
                            onEdit = { showPickerForIndex = idx },
                            onDelayChange = { newDelay ->
                                steps = steps.toMutableList().also { it[idx] = it[idx].copy(delayAfterMs = newDelay) }
                            },
                            onDelete = {
                                steps = steps.toMutableList().also { it.removeAt(idx) }
                            },
                            onMoveUp = {
                                if (idx > 0) steps = steps.toMutableList().also {
                                    val tmp = it[idx]; it[idx] = it[idx - 1]; it[idx - 1] = tmp
                                }
                            },
                            onMoveDown = {
                                if (idx < steps.lastIndex) steps = steps.toMutableList().also {
                                    val tmp = it[idx]; it[idx] = it[idx + 1]; it[idx + 1] = tmp
                                }
                            },
                        )
                    }
                    item {
                        // Bouton ajouter une étape
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GarlemaldColors.ScreenGreenDim, RoundedCornerShape(2.dp))
                                .clickable { showPickerForIndex = -1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text  = "+ AJOUTER COMMANDE",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 10.sp,
                                    color    = GarlemaldColors.ScreenGreen,
                                ),
                            )
                        }
                    }
                }

                HorizontalDivider(color = GarlemaldColors.Border)

                // ── Actions ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text     = "ANNULER",
                        style    = MaterialTheme.typography.labelLarge.copy(
                            color    = GarlemaldColors.MetalLight,
                            fontSize = 10.sp,
                        ),
                        modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp),
                    )
                    Text(
                        text     = "CONFIRMER >",
                        style    = MaterialTheme.typography.labelLarge.copy(
                            color    = if (steps.isNotEmpty()) GarlemaldColors.ImperialRed else GarlemaldColors.MetalDark,
                            fontSize = 10.sp,
                        ),
                        modifier = Modifier
                            .clickable(enabled = steps.isNotEmpty()) {
                                onConfirm(ButtonAssignment.Macro(name = name, steps = steps.toList(), loop = loop))
                            }
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

// ── Ligne d'une étape de macro ────────────────────────────────────────────────

@Composable
private fun MacroStepRow(
    index        : Int,
    step         : MacroStep,
    onEdit       : () -> Unit,
    onDelayChange: (Long) -> Unit,
    onDelete     : () -> Unit,
    onMoveUp     : () -> Unit,
    onMoveDown   : () -> Unit,
) {
    var delayText by remember(step.delayAfterMs) { mutableStateOf(step.delayAfterMs.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .border(1.dp, GarlemaldColors.Border)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Numéro + description + edit
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text  = "${index + 1}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 12.sp,
                    color    = GarlemaldColors.ScreenGreen,
                ),
                modifier = Modifier.width(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = step.command.shortLabel(),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp),
                )
                Text(
                    text  = step.command.displayDescription(),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 8.sp),
                )
            }
            // Flèches + edit + supprimer
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("↑" to onMoveUp, "↓" to onMoveDown, "✎" to onEdit, "✕" to onDelete).forEach { (sym, action) ->
                    Text(
                        text     = sym,
                        style    = MaterialTheme.typography.labelLarge.copy(
                            color    = if (sym == "✕") GarlemaldColors.ImperialRed else GarlemaldColors.MetalLight,
                            fontSize = 12.sp,
                        ),
                        modifier = Modifier
                            .clickable(onClick = action)
                            .padding(4.dp),
                    )
                }
            }
        }
        // Délai après
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text  = "DÉLAI (ms):",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 8.sp),
            )
            GarlemaldTextField(
                value    = delayText,
                label    = "",
                onChange = { v ->
                    delayText = v.filter { it.isDigit() }.take(6)
                    onDelayChange(delayText.toLongOrNull() ?: 500L)
                },
                modifier = Modifier.width(100.dp),
            )
        }
    }
}

// ── TextField style Garlemald ─────────────────────────────────────────────────

@Composable
fun GarlemaldTextField(
    value   : String,
    label   : String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = if (label.isNotBlank()) {{ Text(label, style = MaterialTheme.typography.labelMedium) }} else null,
        textStyle     = MaterialTheme.typography.displayMedium.copy(
            fontSize  = 11.sp,
            color     = GarlemaldColors.ScreenGreen,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        ),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GarlemaldColors.ScreenGreenDim,
            unfocusedBorderColor = GarlemaldColors.Border,
            focusedLabelColor    = GarlemaldColors.ScreenGreenDim,
            unfocusedLabelColor  = GarlemaldColors.MetalDark,
            cursorColor          = GarlemaldColors.ScreenGreen,
        ),
        singleLine    = true,
        modifier      = modifier,
    )
}

// ── Extension helper ──────────────────────────────────────────────────────────

fun CommandSpec.displayDescription() =
    "${moduleName()} › ${submoduleName()} › ${actionName()}"

fun ButtonAssignment.displayLabel(customLabel: String?): String {
    if (!customLabel.isNullOrBlank()) return customLabel
    return when (this) {
        is ButtonAssignment.SingleCommand -> command.shortLabel()
        is ButtonAssignment.Macro        -> name.take(8).uppercase()
    }
}

fun ButtonAssignment.displayDescription(): String = when (this) {
    is ButtonAssignment.SingleCommand ->
        "${command.moduleName()} › ${command.submoduleName()} › ${command.actionName()}"
    is ButtonAssignment.Macro ->
        "MACRO · ${steps.size} cmds${if (loop) " · LOOP" else ""}"
}
