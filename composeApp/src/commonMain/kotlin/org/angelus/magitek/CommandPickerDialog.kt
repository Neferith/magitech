package org.angelus.magitek// commonMain/kotlin/org/angelus/magitek/ui/CommandPickerDialog.kt

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import org.angelus.magitek.model.CommandSpec
import org.angelus.magitek.model.MagitekModules

private enum class PickerStep { MODULE, SUBMODULE, ACTION }

/**
 * Picker 3 étapes : Module → Sous-module → Action.
 * Retourne un CommandSpec ou null (annulation).
 */
@Composable
fun CommandPickerDialog(
    initial:   CommandSpec? = null,
    onConfirm: (CommandSpec) -> Unit,
    onDismiss: () -> Unit,
) {
    var step          by remember { mutableStateOf(PickerStep.MODULE) }
    var selModule     by remember { mutableStateOf(initial?.moduleCode?.let { MagitekModules.byCode[it] }) }
    var selSubmodule  by remember { mutableStateOf(initial?.submoduleCode) }
    var selAction     by remember { mutableStateOf(initial?.actionCode) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color  = GarlemaldColors.Surface,
            border = BorderStroke(1.dp, GarlemaldColors.ImperialRed),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
        ) {
            Column(modifier = Modifier.padding(0.dp)) {
                // En-tête
                PickerHeader(step = step, selModule = selModule, selSubmodule = selSubmodule)

                HorizontalDivider(color = GarlemaldColors.Border)

                // Contenu selon l'étape
                Box(modifier = Modifier.weight(1f)) {
                    when (step) {
                        PickerStep.MODULE -> ModuleList(
                            selected = selModule,
                            onSelect = { mod ->
                                selModule    = mod
                                selSubmodule = null
                                selAction    = null
                                step         = PickerStep.SUBMODULE
                            },
                        )
                        PickerStep.SUBMODULE -> SubmoduleList(
                            module   = selModule!!,
                            selected = selSubmodule,
                            onSelect = { code ->
                                selSubmodule = code
                                selAction    = null
                                step         = PickerStep.ACTION
                            },
                        )
                        PickerStep.ACTION -> ActionList(
                            module   = selModule!!,
                            selected = selAction,
                            onSelect = { code ->
                                selAction = code
                                // Confirmation auto dès l'action sélectionnée
                                onConfirm(
                                    CommandSpec(
                                        moduleCode    = selModule!!.code,
                                        submoduleCode = selSubmodule!!,
                                        actionCode    = code,
                                        freqOverride  = initial?.freqOverride ?: 0L,
                                    )
                                )
                            },
                        )
                    }
                }

                HorizontalDivider(color = GarlemaldColors.Border)

                // Boutons de navigation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (step != PickerStep.MODULE) {
                        PickerNavButton("< RETOUR") {
                            step = when (step) {
                                PickerStep.SUBMODULE -> PickerStep.MODULE
                                PickerStep.ACTION    -> PickerStep.SUBMODULE
                                else                 -> PickerStep.MODULE
                            }
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    PickerNavButton("ANNULER", color = GarlemaldColors.MetalLight, onClick = onDismiss)
                }
            }
        }
    }
}

// ── En-tête avec breadcrumb ───────────────────────────────────────────────────

@Composable
private fun PickerHeader(
    step        : PickerStep,
    selModule   : MagitekModules.ModuleDef?,
    selSubmodule: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GarlemaldColors.SurfaceVariant)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = "SÉLECTION COMMANDE",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text  = when (step) {
                PickerStep.MODULE    -> "> MODULE..."
                PickerStep.SUBMODULE -> "> ${selModule?.name?.uppercase()} › SOUS-MODULE..."
                PickerStep.ACTION    -> "> ${selModule?.name?.uppercase()} › ${
                    selModule?.submodules?.get(selSubmodule)?.uppercase()
                } › ACTION..."
            },
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 10.sp),
        )
    }
}

// ── Listes ────────────────────────────────────────────────────────────────────

@Composable
private fun ModuleList(
    selected: MagitekModules.ModuleDef?,
    onSelect: (MagitekModules.ModuleDef) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(MagitekModules.all) { mod ->
            PickerRow(
                code       = mod.code,
                label      = mod.name,
                isSelected = mod == selected,
                onClick    = { onSelect(mod) },
            )
        }
    }
}

@Composable
private fun SubmoduleList(
    module  : MagitekModules.ModuleDef,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    val entries = module.submodules.entries.toList()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(entries) { (code, label) ->
            PickerRow(
                code       = code,
                label      = label,
                isSelected = code == selected,
                onClick    = { onSelect(code) },
            )
        }
    }
}

@Composable
private fun ActionList(
    module  : MagitekModules.ModuleDef,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    val entries = module.actions.entries.toList()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(entries) { (code, label) ->
            PickerRow(
                code       = code,
                label      = label,
                isSelected = code == selected,
                onClick    = { onSelect(code) },
            )
        }
    }
}

@Composable
private fun PickerRow(
    code       : String,
    label      : String,
    isSelected : Boolean,
    onClick    : () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) GarlemaldColors.ImperialRedDark else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text  = code,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 10.sp,
                color    = if (isSelected) GarlemaldColors.ScreenGreen else GarlemaldColors.ScreenGreenDim,
            ),
        )
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium.copy(
                color = if (isSelected) GarlemaldColors.OnImperialRed else GarlemaldColors.OnSurface,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    HorizontalDivider(color = GarlemaldColors.Border.copy(alpha = 0.3f))
}

@Composable
private fun PickerNavButton(
    label  : String,
    color  : Color = GarlemaldColors.ImperialRed,
    onClick: () -> Unit,
) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelLarge.copy(color = color, fontSize = 10.sp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}