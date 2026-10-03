package com.example.voltduration.ui.screens.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltduration.data.model.CapacityUnit
import com.example.voltduration.data.model.LoadMode
import com.example.voltduration.domain.Formatters
import com.example.voltduration.ui.components.BatteryVisualIndicator
import com.example.voltduration.ui.viewmodel.CalculatorUiState
import com.example.voltduration.ui.viewmodel.VoltDurationViewModel
import java.math.BigDecimal

@Composable
fun CalculatorScreen(
    viewModel: VoltDurationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val triggerHaptic = {
        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isWideScreen) {
            // Two-column layout for tablets and landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Inputs
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculatorHeader()

                    BatteryInputSection(
                        uiState = uiState,
                        onVoltageChanged = viewModel::onVoltageChanged,
                        onCapacityChanged = viewModel::onCapacityChanged,
                        onCapacityUnitChanged = {
                            triggerHaptic()
                            viewModel.onCapacityUnitChanged(it)
                        },
                        onVoltagePreset = {
                            triggerHaptic()
                            viewModel.setVoltagePreset(it)
                        },
                        onCapacityPreset = {
                            triggerHaptic()
                            viewModel.setCapacityPreset(it)
                        }
                    )

                    LoadInputSection(
                        uiState = uiState,
                        onLoadModeChanged = {
                            triggerHaptic()
                            viewModel.onLoadModeChanged(it)
                        },
                        onLoadValueChanged = viewModel::onLoadValueChanged,
                        onLoadPreset = {
                            triggerHaptic()
                            viewModel.setLoadPreset(it)
                        }
                    )

                    EfficiencySection(
                        uiState = uiState,
                        onEfficiencyChanged = viewModel::onEfficiencyChanged,
                        onEfficiencyPreset = {
                            triggerHaptic()
                            viewModel.setEfficiencyPreset(it)
                        }
                    )

                    UsableCapacitySection(
                        uiState = uiState,
                        onUsableCapacityChanged = viewModel::onUsableCapacityChanged,
                        onUsableCapacityPreset = {
                            triggerHaptic()
                            viewModel.setUsableCapacityPreset(it)
                        }
                    )

                    ActionsSection(
                        onCalculate = {
                            focusManager.clearFocus()
                            triggerHaptic()
                            viewModel.calculateExplicit()
                        },
                        onSave = {
                            focusManager.clearFocus()
                            triggerHaptic()
                            viewModel.saveCurrentCalculation()
                        },
                        onReset = {
                            focusManager.clearFocus()
                            triggerHaptic()
                            viewModel.resetToDefaults()
                        },
                        canSave = uiState.result != null && uiState.validation.isValid
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Right Column: Live Results & Summary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ResultSection(
                        uiState = uiState,
                        showEnergyBreakdown = settings.showEnergyBreakdown
                    )

                    BatterySummaryCard(uiState = uiState)

                    if (settings.showLimitations) {
                        LimitationNoticeCard()
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Single-column scrolling layout for portrait phones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CalculatorHeader()

                BatteryInputSection(
                    uiState = uiState,
                    onVoltageChanged = viewModel::onVoltageChanged,
                    onCapacityChanged = viewModel::onCapacityChanged,
                    onCapacityUnitChanged = {
                        triggerHaptic()
                        viewModel.onCapacityUnitChanged(it)
                    },
                    onVoltagePreset = {
                        triggerHaptic()
                        viewModel.setVoltagePreset(it)
                    },
                    onCapacityPreset = {
                        triggerHaptic()
                        viewModel.setCapacityPreset(it)
                    }
                )

                LoadInputSection(
                    uiState = uiState,
                    onLoadModeChanged = {
                        triggerHaptic()
                        viewModel.onLoadModeChanged(it)
                    },
                    onLoadValueChanged = viewModel::onLoadValueChanged,
                    onLoadPreset = {
                        triggerHaptic()
                        viewModel.setLoadPreset(it)
                    }
                )

                EfficiencySection(
                    uiState = uiState,
                    onEfficiencyChanged = viewModel::onEfficiencyChanged,
                    onEfficiencyPreset = {
                        triggerHaptic()
                        viewModel.setEfficiencyPreset(it)
                    }
                )

                UsableCapacitySection(
                    uiState = uiState,
                    onUsableCapacityChanged = viewModel::onUsableCapacityChanged,
                    onUsableCapacityPreset = {
                        triggerHaptic()
                        viewModel.setUsableCapacityPreset(it)
                    }
                )

                ActionsSection(
                    onCalculate = {
                        focusManager.clearFocus()
                        triggerHaptic()
                        viewModel.calculateExplicit()
                    },
                    onSave = {
                        focusManager.clearFocus()
                        triggerHaptic()
                        viewModel.saveCurrentCalculation()
                    },
                    onReset = {
                        focusManager.clearFocus()
                        triggerHaptic()
                        viewModel.resetToDefaults()
                    },
                    canSave = uiState.result != null && uiState.validation.isValid
                )

                // Results
                ResultSection(
                    uiState = uiState,
                    showEnergyBreakdown = settings.showEnergyBreakdown
                )

                BatterySummaryCard(uiState = uiState)

                if (settings.showLimitations) {
                    LimitationNoticeCard()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CalculatorHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .semantics { heading() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "VOLTDURATION",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "Estimate battery runtime and energy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryInputSection(
    uiState: CalculatorUiState,
    onVoltageChanged: (String) -> Unit,
    onCapacityChanged: (String) -> Unit,
    onCapacityUnitChanged: (CapacityUnit) -> Unit,
    onVoltagePreset: (String) -> Unit,
    onCapacityPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "BATTERY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Battery Voltage Input
            OutlinedTextField(
                value = uiState.voltage,
                onValueChange = onVoltageChanged,
                label = { Text("Battery Voltage") },
                placeholder = { Text("12.0") },
                suffix = { Text("V", fontWeight = FontWeight.SemiBold) },
                isError = uiState.validation.voltageError != null,
                supportingText = uiState.validation.voltageError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Battery voltage" }
            )

            // Voltage Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("12", "24", "36", "48").forEach { v ->
                    val isSelected = uiState.voltage == v || uiState.voltage == "$v.0"
                    FilterChip(
                        selected = isSelected,
                        onClick = { onVoltagePreset(v) },
                        label = { Text("${v} V") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp
            )

            // Capacity Unit Toggle (mAh / Ah)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                CapacityUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = uiState.capacityUnit == unit,
                        onClick = { onCapacityUnitChanged(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CapacityUnit.entries.size)
                    ) {
                        Text(unit.symbol)
                    }
                }
            }

            // Battery Capacity Input
            OutlinedTextField(
                value = uiState.capacity,
                onValueChange = onCapacityChanged,
                label = { Text("Battery Capacity") },
                placeholder = { Text(if (uiState.capacityUnit == CapacityUnit.AH) "100" else "5000") },
                suffix = { Text(uiState.capacityUnit.symbol, fontWeight = FontWeight.SemiBold) },
                isError = uiState.validation.capacityError != null,
                supportingText = uiState.validation.capacityError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Battery capacity in ${uiState.capacityUnit.displayName}" }
            )

            // Capacity Presets (shown in Ah reference, automatically converted by handler)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("5", "10", "20", "50", "100", "200").forEach { capAh ->
                    val label = if (uiState.capacityUnit == CapacityUnit.MAH) {
                        "${(capAh.toInt() * 1000)} mAh"
                    } else {
                        "$capAh Ah"
                    }
                    val isSelected = if (uiState.capacityUnit == CapacityUnit.MAH) {
                        uiState.capacity == (capAh.toInt() * 1000).toString()
                    } else {
                        uiState.capacity == capAh
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCapacityPreset(capAh) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadInputSection(
    uiState: CalculatorUiState,
    onLoadModeChanged: (LoadMode) -> Unit,
    onLoadValueChanged: (String) -> Unit,
    onLoadPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LOAD",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Load Mode Selector (LOAD POWER / CURRENT)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                LoadMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = uiState.loadMode == mode,
                        onClick = { onLoadModeChanged(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = LoadMode.entries.size)
                    ) {
                        Text(if (mode == LoadMode.WATTS) "LOAD POWER (W)" else "CURRENT (A)")
                    }
                }
            }

            // Input field depends on mode
            if (uiState.loadMode == LoadMode.WATTS) {
                OutlinedTextField(
                    value = uiState.loadValue,
                    onValueChange = onLoadValueChanged,
                    label = { Text("Load Power") },
                    placeholder = { Text("100") },
                    suffix = { Text("W", fontWeight = FontWeight.SemiBold) },
                    isError = uiState.validation.loadError != null,
                    supportingText = uiState.validation.loadError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Load power in Watts" }
                )

                // Quick load presets for Watts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("10", "50", "100", "250", "500", "1000").forEach { w ->
                        val isSelected = uiState.loadValue == w
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLoadPreset(w) },
                            label = { Text("${w} W") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.loadValue,
                    onValueChange = onLoadValueChanged,
                    label = { Text("Current") },
                    placeholder = { Text("8.0") },
                    suffix = { Text("A", fontWeight = FontWeight.SemiBold) },
                    isError = uiState.validation.loadError != null,
                    supportingText = uiState.validation.loadError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Current in Amps" }
                )

                // Amp presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1", "2", "5", "8", "10", "20").forEach { a ->
                        val isSelected = uiState.loadValue == a
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLoadPreset(a) },
                            label = { Text("${a} A") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Estimated Load Power Callout for Amp Mode
                if (uiState.estimatedLoadWatts != null && uiState.estimatedLoadWatts > BigDecimal.ZERO) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated load power:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatPowerWatts(uiState.estimatedLoadWatts),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Estimated from entered voltage (${uiState.voltage} V × ${uiState.loadValue} A)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EfficiencySection(
    uiState: CalculatorUiState,
    onEfficiencyChanged: (String) -> Unit,
    onEfficiencyPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SYSTEM EFFICIENCY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = uiState.efficiency,
                onValueChange = onEfficiencyChanged,
                label = { Text("Efficiency") },
                placeholder = { Text("90") },
                suffix = { Text("%", fontWeight = FontWeight.SemiBold) },
                isError = uiState.validation.efficiencyError != null,
                supportingText = {
                    Text(
                        uiState.validation.efficiencyError
                            ?: "Accounts for conversion and system losses."
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "System efficiency percentage" }
            )

            // Quick chips: 70%, 80%, 85%, 90%, 95%, 100%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("70", "80", "85", "90", "95", "100").forEach { eff ->
                    val isSelected = uiState.efficiency == eff
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEfficiencyPreset(eff) },
                        label = { Text("$eff%") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun UsableCapacitySection(
    uiState: CalculatorUiState,
    onUsableCapacityChanged: (String) -> Unit,
    onUsableCapacityPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "USABLE CAPACITY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = uiState.usableCapacity,
                onValueChange = onUsableCapacityChanged,
                label = { Text("Usable Capacity") },
                placeholder = { Text("100") },
                suffix = { Text("%", fontWeight = FontWeight.SemiBold) },
                isError = uiState.validation.usableCapacityError != null,
                supportingText = {
                    Text(
                        uiState.validation.usableCapacityError
                            ?: "Estimated portion of nominal battery capacity used."
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Usable battery capacity percentage" }
            )

            // Quick chips: 50%, 60%, 70%, 80%, 90%, 100%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("50", "60", "70", "80", "90", "100").forEach { use ->
                    val isSelected = uiState.usableCapacity == use
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUsableCapacityPreset(use) },
                        label = { Text("$use%") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    onCalculate: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    canSave: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Primary CALCULATE Button
        Button(
            onClick = onCalculate,
            modifier = Modifier.weight(1.35f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("CALCULATE", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 12.sp)
        }

        // SAVE Button
        OutlinedButton(
            onClick = onSave,
            enabled = canSave,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 10.dp),
            modifier = Modifier.weight(0.9f)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save", maxLines = 1)
        }

        // RESET Button
        OutlinedButton(
            onClick = onReset,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 10.dp),
            modifier = Modifier.weight(0.9f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset", maxLines = 1)
        }
    }
}

@Composable
private fun ResultSection(
    uiState: CalculatorUiState,
    showEnergyBreakdown: Boolean
) {
    val result = uiState.result

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Estimated Runtime - Visually Dominant
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESTIMATED RUNTIME",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (result != null) {
                    Text(
                        text = uiState.runtimeFormatted,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics {
                            contentDescription = "Estimated runtime: ${uiState.runtimeFormatted}"
                        }
                    )

                    if (uiState.decimalHoursFormatted.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.decimalHoursFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "--",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enter valid battery and load values",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Key Results Metrics Grid
            if (result != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Battery Energy
                    MetricBox(
                        title = "BATTERY ENERGY",
                        primaryValue = uiState.nominalEnergyWhFormatted,
                        secondaryValue = uiState.nominalEnergyKwhFormatted,
                        modifier = Modifier.weight(1f)
                    )

                    // Effective Energy
                    MetricBox(
                        title = "EFFECTIVE ENERGY",
                        primaryValue = uiState.effectiveEnergyKwhFormatted,
                        secondaryValue = uiState.effectiveEnergyWhFormatted,
                        modifier = Modifier.weight(1f)
                    )

                    // Load
                    MetricBox(
                        title = "LOAD",
                        primaryValue = Formatters.formatPowerWatts(result.loadWatts),
                        secondaryValue = if (uiState.loadMode == LoadMode.AMPS) "${uiState.loadValue} A" else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Small Horizontal Battery Visual Indicator
                BatteryVisualIndicator(
                    nominalEnergyWh = result.nominalEnergyWh,
                    effectiveEnergyWh = result.effectiveEnergyWh
                )

                // Energy Breakdown Section
                if (showEnergyBreakdown) {
                    EnergyBreakdownCard(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    primaryValue: String,
    secondaryValue: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (secondaryValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = secondaryValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnergyBreakdownCard(uiState: CalculatorUiState) {
    val result = uiState.result ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ENERGY BREAKDOWN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        BreakdownRow(
            label = "NOMINAL ENERGY",
            value = Formatters.formatEnergyKwhFixed3(result.nominalEnergyWh)
        )
        BreakdownRow(
            label = "USABLE ENERGY (${uiState.usableCapacity}%)",
            value = Formatters.formatEnergyKwhFixed3(result.usableEnergyWh)
        )
        BreakdownRow(
            label = "AFTER EFFICIENCY (${uiState.efficiency}%)",
            value = Formatters.formatEnergyKwhFixed3(result.effectiveEnergyWh)
        )
        BreakdownRow(
            label = "ESTIMATED RUNTIME",
            value = uiState.runtimeFormatted,
            isHighlight = true
        )
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun BatterySummaryCard(uiState: CalculatorUiState) {
    val result = uiState.result ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "BATTERY SUMMARY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryCol("VOLTAGE", "${uiState.voltage} V")
                SummaryCol("CAPACITY", "${uiState.capacity} ${uiState.capacityUnit.symbol}")
                SummaryCol("ENERGY", uiState.nominalEnergyKwhFormatted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryCol("USABLE", "${uiState.usableCapacity}%")
                SummaryCol("EFFICIENCY", "${uiState.efficiency}%")
                SummaryCol("LOAD", Formatters.formatPowerWatts(result.loadWatts))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RUNTIME",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = uiState.runtimeFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCol(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LimitationNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ESTIMATION NOTICE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Runtime is an estimate. Actual results vary with battery condition, load, temperature, efficiency, and discharge characteristics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Actual runtime can differ because battery voltage, load, temperature, efficiency, and discharge characteristics change during operation.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
