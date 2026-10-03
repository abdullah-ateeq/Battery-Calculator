package com.example.voltduration.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

object Formatters {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat2 = DecimalFormat("#,##0.00", symbols)
    private val decimalFormat3 = DecimalFormat("#,##0.000", symbols)
    private val integerFormat = DecimalFormat("#,##0", symbols)
    private val decimalOptionalFormat = DecimalFormat("#,##0.##", symbols)

    fun formatRuntime(hours: BigDecimal): String {
        if (hours <= BigDecimal.ZERO) return "0 min"

        // Total minutes rounded to nearest whole minute
        val totalMinutesDouble = hours.multiply(BigDecimal(60)).toDouble()
        val totalMinutes = totalMinutesDouble.roundToLong()

        if (totalMinutes < 1) {
            return "< 1 min"
        }

        val days = totalMinutes / (24 * 60)
        val remainingMinutesAfterDays = totalMinutes % (24 * 60)
        val h = remainingMinutesAfterDays / 60
        val m = remainingMinutesAfterDays % 60

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days} d")
        if (h > 0) parts.add("${h} h")
        if (m > 0) parts.add("${m} min")

        // In edge case where totalMinutes was e.g. 0 after round
        return if (parts.isEmpty()) "0 min" else parts.joinToString(" ")
    }

    fun formatDecimalHours(hours: BigDecimal): String {
        val rounded = hours.setScale(2, RoundingMode.HALF_UP)
        return "${decimalFormat2.format(rounded)} hours"
    }

    fun formatEnergyWh(energyWh: BigDecimal): String {
        val rounded = energyWh.setScale(1, RoundingMode.HALF_UP)
        return if (rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            "${integerFormat.format(rounded)} Wh"
        } else {
            "${decimalOptionalFormat.format(rounded)} Wh"
        }
    }

    fun formatEnergyKwh(energyWh: BigDecimal): String {
        val kwh = energyWh.divide(BigDecimal(1000), 3, RoundingMode.HALF_UP)
        return "${decimalFormat3.format(kwh).trimEnd('0').trimEnd('.').let { if (!it.contains('.')) "$it.0" else it }} kWh"
    }

    fun formatEnergyKwhFixed3(energyWh: BigDecimal): String {
        val kwh = energyWh.divide(BigDecimal(1000), 3, RoundingMode.HALF_UP)
        return "${decimalFormat3.format(kwh)} kWh"
    }

    fun formatPowerWatts(watts: BigDecimal): String {
        val rounded = watts.setScale(1, RoundingMode.HALF_UP)
        return if (rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            "${integerFormat.format(rounded)} W"
        } else {
            "${decimalOptionalFormat.format(rounded)} W"
        }
    }

    fun formatCurrentAmps(amps: BigDecimal): String {
        return "${decimalOptionalFormat.format(amps)} A"
    }

    fun formatVoltage(voltage: BigDecimal): String {
        return "${decimalOptionalFormat.format(voltage)} V"
    }

    fun formatCapacity(capacity: BigDecimal, unit: String): String {
        return "${decimalOptionalFormat.format(capacity)} $unit"
    }

    fun formatPercent(percent: BigDecimal): String {
        return "${decimalOptionalFormat.format(percent)}%"
    }
}
