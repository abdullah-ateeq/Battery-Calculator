package com.example.voltduration.data.model

import java.math.BigDecimal

data class BatteryCalculationInput(
    val voltage: BigDecimal,
    val capacity: BigDecimal,
    val capacityUnit: CapacityUnit,
    val loadValue: BigDecimal,
    val loadMode: LoadMode,
    val efficiencyPercent: BigDecimal,
    val usableCapacityPercent: BigDecimal
)

data class BatteryCalculationResult(
    val capacityAh: BigDecimal,
    val loadWatts: BigDecimal,
    val nominalEnergyWh: BigDecimal,
    val usableEnergyWh: BigDecimal,
    val effectiveEnergyWh: BigDecimal,
    val runtimeHours: BigDecimal
)

data class CalculationHistoryItem(
    val id: String,
    val timestamp: Long,
    val formattedDate: String,
    val voltageV: String,
    val capacity: String,
    val capacityUnit: CapacityUnit,
    val loadMode: LoadMode,
    val loadValue: String,
    val estimatedLoadWatts: String,
    val efficiencyPercent: String,
    val usableCapacityPercent: String,
    val nominalEnergyWh: String,
    val usableEnergyWh: String,
    val effectiveEnergyWh: String,
    val runtimeHours: String,
    val runtimeFormatted: String
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultVoltage: String = "12",
    val defaultCapacity: String = "100",
    val defaultCapacityUnit: CapacityUnit = CapacityUnit.AH,
    val defaultLoadMode: LoadMode = LoadMode.WATTS,
    val defaultLoadValue: String = "100",
    val defaultEfficiency: String = "90",
    val defaultUsableCapacity: String = "100",
    val hapticFeedback: Boolean = true,
    val showEnergyBreakdown: Boolean = true,
    val showLimitations: Boolean = true
)
