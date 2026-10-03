package com.example.voltduration.domain

import com.example.voltduration.data.model.BatteryCalculationInput
import com.example.voltduration.data.model.BatteryCalculationResult
import com.example.voltduration.data.model.CapacityUnit
import com.example.voltduration.data.model.LoadMode
import java.math.BigDecimal
import java.math.RoundingMode

object CalculatorEngine {

    val MIN_VOLTAGE: BigDecimal = BigDecimal("0.1")
    val MAX_VOLTAGE: BigDecimal = BigDecimal("1000.0")
    val MIN_EFFICIENCY: BigDecimal = BigDecimal("50.0")
    val MAX_EFFICIENCY: BigDecimal = BigDecimal("100.0")
    val MIN_USABLE_CAPACITY: BigDecimal = BigDecimal("1.0")
    val MAX_USABLE_CAPACITY: BigDecimal = BigDecimal("100.0")

    private val ONE_THOUSAND = BigDecimal("1000")
    private val ONE_HUNDRED = BigDecimal("100")

    fun parseSafeBigDecimal(input: String?): BigDecimal? {
        if (input.isNullOrBlank()) return null
        return try {
            val sanitized = input.trim().replace(',', '.')
            val parsed = BigDecimal(sanitized)
            if (parsed.signum() < 0) null else parsed
        } catch (_: Exception) {
            null
        }
    }

    fun parseAnyBigDecimal(input: String?): BigDecimal? {
        if (input.isNullOrBlank()) return null
        return try {
            val sanitized = input.trim().replace(',', '.')
            BigDecimal(sanitized)
        } catch (_: Exception) {
            null
        }
    }

    fun validateVoltage(voltage: BigDecimal?): String? {
        if (voltage == null) return "Enter a valid battery voltage."
        if (voltage <= BigDecimal.ZERO || voltage < MIN_VOLTAGE || voltage > MAX_VOLTAGE) {
            return "Enter a valid battery voltage."
        }
        return null
    }

    fun validateCapacity(capacity: BigDecimal?): String? {
        if (capacity == null || capacity <= BigDecimal.ZERO) {
            return "Enter a valid battery capacity."
        }
        return null
    }

    fun validateLoad(load: BigDecimal?): String? {
        if (load == null || load <= BigDecimal.ZERO) {
            return "Load must be greater than zero."
        }
        return null
    }

    fun validateEfficiency(efficiency: BigDecimal?): String? {
        if (efficiency == null || efficiency < MIN_EFFICIENCY || efficiency > MAX_EFFICIENCY) {
            return "Efficiency must be between 50% and 100%."
        }
        return null
    }

    fun validateUsableCapacity(usableCapacity: BigDecimal?): String? {
        if (usableCapacity == null || usableCapacity < MIN_USABLE_CAPACITY || usableCapacity > MAX_USABLE_CAPACITY) {
            return "Usable capacity must be between 1% and 100%."
        }
        return null
    }

    fun capacityToAh(value: BigDecimal, unit: CapacityUnit): BigDecimal {
        return when (unit) {
            CapacityUnit.AH -> value
            CapacityUnit.MAH -> value.divide(ONE_THOUSAND, 10, RoundingMode.HALF_UP)
        }
    }

    fun convertCapacityUnit(value: BigDecimal, fromUnit: CapacityUnit, toUnit: CapacityUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        return if (fromUnit == CapacityUnit.MAH && toUnit == CapacityUnit.AH) {
            value.divide(ONE_THOUSAND, 6, RoundingMode.HALF_UP).stripTrailingZeros()
        } else {
            value.multiply(ONE_THOUSAND).stripTrailingZeros()
        }
    }

    fun calculateEnergyWh(voltage: BigDecimal, capacityAh: BigDecimal): BigDecimal {
        return voltage.multiply(capacityAh)
    }

    fun calculateLoadWatts(voltage: BigDecimal, loadValue: BigDecimal, loadMode: LoadMode): BigDecimal {
        return when (loadMode) {
            LoadMode.WATTS -> loadValue
            LoadMode.AMPS -> voltage.multiply(loadValue)
        }
    }

    fun calculateEffectiveEnergy(
        nominalEnergyWh: BigDecimal,
        usableCapacityPercent: BigDecimal,
        efficiencyPercent: BigDecimal
    ): Pair<BigDecimal, BigDecimal> {
        val usableEnergy = nominalEnergyWh.multiply(usableCapacityPercent).divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP)
        val effectiveEnergy = usableEnergy.multiply(efficiencyPercent).divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP)
        return Pair(usableEnergy, effectiveEnergy)
    }

    fun calculateRuntimeHours(effectiveEnergyWh: BigDecimal, loadWatts: BigDecimal): BigDecimal {
        if (loadWatts <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Load must be greater than zero.")
        }
        return effectiveEnergyWh.divide(loadWatts, 12, RoundingMode.HALF_UP)
    }

    fun calculate(input: BatteryCalculationInput): BatteryCalculationResult {
        val capacityAh = capacityToAh(input.capacity, input.capacityUnit)
        val nominalEnergyWh = calculateEnergyWh(input.voltage, capacityAh)
        val (usableEnergyWh, effectiveEnergyWh) = calculateEffectiveEnergy(
            nominalEnergyWh = nominalEnergyWh,
            usableCapacityPercent = input.usableCapacityPercent,
            efficiencyPercent = input.efficiencyPercent
        )
        val loadWatts = calculateLoadWatts(
            voltage = input.voltage,
            loadValue = input.loadValue,
            loadMode = input.loadMode
        )
        val runtimeHours = calculateRuntimeHours(effectiveEnergyWh, loadWatts)

        return BatteryCalculationResult(
            capacityAh = capacityAh,
            loadWatts = loadWatts,
            nominalEnergyWh = nominalEnergyWh,
            usableEnergyWh = usableEnergyWh,
            effectiveEnergyWh = effectiveEnergyWh,
            runtimeHours = runtimeHours
        )
    }
}

data class CalculationValidation(
    val voltageError: String? = null,
    val capacityError: String? = null,
    val loadError: String? = null,
    val efficiencyError: String? = null,
    val usableCapacityError: String? = null
) {
    val isValid: Boolean
        get() = voltageError == null &&
                capacityError == null &&
                loadError == null &&
                efficiencyError == null &&
                usableCapacityError == null
}
