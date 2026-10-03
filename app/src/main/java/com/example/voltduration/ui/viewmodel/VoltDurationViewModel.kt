package com.example.voltduration.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltduration.data.model.AppSettings
import com.example.voltduration.data.model.BatteryCalculationInput
import com.example.voltduration.data.model.BatteryCalculationResult
import com.example.voltduration.data.model.CalculationHistoryItem
import com.example.voltduration.data.model.CapacityUnit
import com.example.voltduration.data.model.LoadMode
import com.example.voltduration.data.model.ThemeMode
import com.example.voltduration.data.storage.PreferencesManager
import com.example.voltduration.domain.CalculationValidation
import com.example.voltduration.domain.CalculatorEngine
import com.example.voltduration.domain.Formatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CalculatorUiState(
    val voltage: String = "12",
    val capacity: String = "100",
    val capacityUnit: CapacityUnit = CapacityUnit.AH,
    val loadMode: LoadMode = LoadMode.WATTS,
    val loadValue: String = "100",
    val efficiency: String = "90",
    val usableCapacity: String = "100",
    val validation: CalculationValidation = CalculationValidation(),
    val result: BatteryCalculationResult? = null,
    val estimatedLoadWatts: BigDecimal? = null,
    val runtimeFormatted: String = "",
    val decimalHoursFormatted: String = "",
    val nominalEnergyWhFormatted: String = "",
    val nominalEnergyKwhFormatted: String = "",
    val usableEnergyWhFormatted: String = "",
    val usableEnergyKwhFormatted: String = "",
    val effectiveEnergyWhFormatted: String = "",
    val effectiveEnergyKwhFormatted: String = "",
    val isSavedSuccess: Boolean = false
)

class VoltDurationViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application.applicationContext)

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val history: StateFlow<List<CalculationHistoryItem>> = preferencesManager.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        // Initialize with default state and perform initial live calculation
        recalculate()
    }

    fun onVoltageChanged(newVoltage: String) {
        _uiState.value = _uiState.value.copy(voltage = newVoltage)
        recalculate()
    }

    fun onCapacityChanged(newCapacity: String) {
        _uiState.value = _uiState.value.copy(capacity = newCapacity)
        recalculate()
    }

    fun onCapacityUnitChanged(newUnit: CapacityUnit) {
        val currentUnit = _uiState.value.capacityUnit
        if (currentUnit == newUnit) return

        val currentVal = CalculatorEngine.parseSafeBigDecimal(_uiState.value.capacity)
        val convertedCapacityString = if (currentVal != null && currentVal > BigDecimal.ZERO) {
            val converted = CalculatorEngine.convertCapacityUnit(currentVal, currentUnit, newUnit)
            converted.toPlainString()
        } else {
            _uiState.value.capacity
        }

        _uiState.value = _uiState.value.copy(
            capacityUnit = newUnit,
            capacity = convertedCapacityString
        )
        recalculate()
    }

    fun onLoadModeChanged(newMode: LoadMode) {
        if (_uiState.value.loadMode == newMode) return
        _uiState.value = _uiState.value.copy(loadMode = newMode)
        recalculate()
    }

    fun onLoadValueChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(loadValue = newValue)
        recalculate()
    }

    fun onEfficiencyChanged(newEfficiency: String) {
        _uiState.value = _uiState.value.copy(efficiency = newEfficiency)
        recalculate()
    }

    fun onUsableCapacityChanged(newUsableCapacity: String) {
        _uiState.value = _uiState.value.copy(usableCapacity = newUsableCapacity)
        recalculate()
    }

    fun setVoltagePreset(preset: String) {
        _uiState.value = _uiState.value.copy(voltage = preset)
        recalculate()
    }

    fun setCapacityPreset(presetAh: String) {
        val targetValue = if (_uiState.value.capacityUnit == CapacityUnit.MAH) {
            val parsedAh = BigDecimal(presetAh)
            parsedAh.multiply(BigDecimal(1000)).stripTrailingZeros().toPlainString()
        } else {
            presetAh
        }
        _uiState.value = _uiState.value.copy(capacity = targetValue)
        recalculate()
    }

    fun setLoadPreset(presetWattsOrAmps: String) {
        _uiState.value = _uiState.value.copy(loadValue = presetWattsOrAmps)
        recalculate()
    }

    fun setEfficiencyPreset(presetPercent: String) {
        _uiState.value = _uiState.value.copy(efficiency = presetPercent)
        recalculate()
    }

    fun setUsableCapacityPreset(presetPercent: String) {
        _uiState.value = _uiState.value.copy(usableCapacity = presetPercent)
        recalculate()
    }

    fun resetToDefaults() {
        val currentSettings = settings.value
        _uiState.value = CalculatorUiState(
            voltage = currentSettings.defaultVoltage,
            capacity = currentSettings.defaultCapacity,
            capacityUnit = currentSettings.defaultCapacityUnit,
            loadMode = currentSettings.defaultLoadMode,
            loadValue = currentSettings.defaultLoadValue,
            efficiency = currentSettings.defaultEfficiency,
            usableCapacity = currentSettings.defaultUsableCapacity
        )
        recalculate()
        emitSnackbar("Calculator reset to defaults")
    }

    fun recalculate() {
        val state = _uiState.value
        val parsedVoltage = CalculatorEngine.parseSafeBigDecimal(state.voltage)
        val parsedCapacity = CalculatorEngine.parseSafeBigDecimal(state.capacity)
        val parsedLoad = CalculatorEngine.parseSafeBigDecimal(state.loadValue)
        val parsedEfficiency = CalculatorEngine.parseSafeBigDecimal(state.efficiency)
        val parsedUsable = CalculatorEngine.parseSafeBigDecimal(state.usableCapacity)

        val vError = CalculatorEngine.validateVoltage(parsedVoltage)
        val cError = CalculatorEngine.validateCapacity(parsedCapacity)
        val lError = CalculatorEngine.validateLoad(parsedLoad)
        val eError = CalculatorEngine.validateEfficiency(parsedEfficiency)
        val uError = CalculatorEngine.validateUsableCapacity(parsedUsable)

        val validation = CalculationValidation(
            voltageError = vError,
            capacityError = cError,
            loadError = lError,
            efficiencyError = eError,
            usableCapacityError = uError
        )

        val estWatts = if (parsedVoltage != null && parsedLoad != null) {
            CalculatorEngine.calculateLoadWatts(parsedVoltage, parsedLoad, state.loadMode)
        } else null

        if (validation.isValid && parsedVoltage != null && parsedCapacity != null &&
            parsedLoad != null && parsedEfficiency != null && parsedUsable != null
        ) {
            try {
                val input = BatteryCalculationInput(
                    voltage = parsedVoltage,
                    capacity = parsedCapacity,
                    capacityUnit = state.capacityUnit,
                    loadValue = parsedLoad,
                    loadMode = state.loadMode,
                    efficiencyPercent = parsedEfficiency,
                    usableCapacityPercent = parsedUsable
                )
                val result = CalculatorEngine.calculate(input)

                _uiState.value = state.copy(
                    validation = validation,
                    result = result,
                    estimatedLoadWatts = estWatts,
                    runtimeFormatted = Formatters.formatRuntime(result.runtimeHours),
                    decimalHoursFormatted = Formatters.formatDecimalHours(result.runtimeHours),
                    nominalEnergyWhFormatted = Formatters.formatEnergyWh(result.nominalEnergyWh),
                    nominalEnergyKwhFormatted = Formatters.formatEnergyKwh(result.nominalEnergyWh),
                    usableEnergyWhFormatted = Formatters.formatEnergyWh(result.usableEnergyWh),
                    usableEnergyKwhFormatted = Formatters.formatEnergyKwh(result.usableEnergyWh),
                    effectiveEnergyWhFormatted = Formatters.formatEnergyWh(result.effectiveEnergyWh),
                    effectiveEnergyKwhFormatted = Formatters.formatEnergyKwh(result.effectiveEnergyWh)
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    validation = validation.copy(loadError = e.message ?: "Calculation error"),
                    result = null,
                    estimatedLoadWatts = estWatts
                )
            }
        } else {
            _uiState.value = state.copy(
                validation = validation,
                result = null,
                estimatedLoadWatts = estWatts
            )
        }
    }

    fun calculateExplicit() {
        recalculate()
        if (_uiState.value.result != null) {
            emitSnackbar("Calculation updated: ${_uiState.value.runtimeFormatted}")
        } else {
            val firstError = _uiState.value.validation.let {
                it.voltageError ?: it.capacityError ?: it.loadError ?: it.efficiencyError ?: it.usableCapacityError
            } ?: "Please check input values."
            emitSnackbar(firstError)
        }
    }

    fun saveCurrentCalculation() {
        val state = _uiState.value
        val result = state.result
        if (result == null || !state.validation.isValid) {
            emitSnackbar("Cannot save incomplete calculation.")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(now))

            val historyItem = CalculationHistoryItem(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                formattedDate = formattedDate,
                voltageV = state.voltage,
                capacity = state.capacity,
                capacityUnit = state.capacityUnit,
                loadMode = state.loadMode,
                loadValue = state.loadValue,
                estimatedLoadWatts = Formatters.formatPowerWatts(result.loadWatts),
                efficiencyPercent = state.efficiency,
                usableCapacityPercent = state.usableCapacity,
                nominalEnergyWh = Formatters.formatEnergyWh(result.nominalEnergyWh),
                usableEnergyWh = Formatters.formatEnergyWh(result.usableEnergyWh),
                effectiveEnergyWh = Formatters.formatEnergyWh(result.effectiveEnergyWh),
                runtimeHours = Formatters.formatDecimalHours(result.runtimeHours),
                runtimeFormatted = state.runtimeFormatted
            )

            preferencesManager.saveCalculation(historyItem)
            emitSnackbar("Calculation saved to History")
        }
    }

    fun restoreCalculation(item: CalculationHistoryItem) {
        _uiState.value = _uiState.value.copy(
            voltage = item.voltageV,
            capacity = item.capacity,
            capacityUnit = item.capacityUnit,
            loadMode = item.loadMode,
            loadValue = item.loadValue,
            efficiency = item.efficiencyPercent,
            usableCapacity = item.usableCapacityPercent
        )
        recalculate()
        emitSnackbar("Restored ${item.voltageV}V / ${item.capacity}${item.capacityUnit.symbol} calculation")
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            preferencesManager.deleteCalculation(id)
            emitSnackbar("Item removed from history")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferencesManager.clearHistory()
            emitSnackbar("All calculation history cleared")
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(themeMode = mode) }
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(dynamicColor = enabled) }
        }
    }

    fun updateDefaultVoltage(voltage: String) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(defaultVoltage = voltage) }
        }
    }

    fun updateDefaultCapacityUnit(unit: CapacityUnit) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(defaultCapacityUnit = unit) }
        }
    }

    fun updateDefaultLoadMode(mode: LoadMode) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(defaultLoadMode = mode) }
        }
    }

    fun updateDefaultEfficiency(efficiency: String) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(defaultEfficiency = efficiency) }
        }
    }

    fun updateDefaultUsableCapacity(usable: String) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(defaultUsableCapacity = usable) }
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(hapticFeedback = enabled) }
        }
    }

    fun updateShowEnergyBreakdown(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(showEnergyBreakdown = enabled) }
        }
    }

    fun updateShowLimitations(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSettings { it.copy(showLimitations = enabled) }
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            preferencesManager.resetSettings()
            emitSnackbar("Settings reset to defaults")
        }
    }

    private fun emitSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarEvent.emit(message)
        }
    }
}
