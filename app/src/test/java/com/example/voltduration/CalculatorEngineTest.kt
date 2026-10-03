package com.example.voltduration

import com.example.voltduration.data.model.BatteryCalculationInput
import com.example.voltduration.data.model.CalculationHistoryItem
import com.example.voltduration.data.model.CapacityUnit
import com.example.voltduration.data.model.LoadMode
import com.example.voltduration.domain.CalculatorEngine
import com.example.voltduration.domain.Formatters
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorEngineTest {

    // 1. mAh -> Ah
    @Test
    fun test1_mahToAhConversion() {
        val inputMah = BigDecimal("5000")
        val resultAh = CalculatorEngine.capacityToAh(inputMah, CapacityUnit.MAH)
        assertEquals(0, BigDecimal("5").compareTo(resultAh.stripTrailingZeros()))

        val inputMah2 = BigDecimal("2500")
        val resultAh2 = CalculatorEngine.capacityToAh(inputMah2, CapacityUnit.MAH)
        assertEquals(0, BigDecimal("2.5").compareTo(resultAh2.stripTrailingZeros()))

        val inputMah3 = BigDecimal("1000")
        val resultAh3 = CalculatorEngine.capacityToAh(inputMah3, CapacityUnit.MAH)
        assertEquals(0, BigDecimal("1").compareTo(resultAh3.stripTrailingZeros()))
    }

    // 2. Ah input
    @Test
    fun test2_ahInput() {
        val inputAh = BigDecimal("100")
        val resultAh = CalculatorEngine.capacityToAh(inputAh, CapacityUnit.AH)
        assertEquals(0, BigDecimal("100").compareTo(resultAh))

        val inputAh2 = BigDecimal("0.5")
        val resultAh2 = CalculatorEngine.capacityToAh(inputAh2, CapacityUnit.AH)
        assertEquals(0, BigDecimal("0.5").compareTo(resultAh2))
    }

    // 3. Energy calculation
    @Test
    fun test3_energyCalculation() {
        val voltage = BigDecimal("12.0")
        val capacityAh = BigDecimal("100")
        val energyWh = CalculatorEngine.calculateEnergyWh(voltage, capacityAh)
        assertEquals(0, BigDecimal("1200.0").compareTo(energyWh))

        val v2 = BigDecimal("48.0")
        val c2 = BigDecimal("50")
        val e2 = CalculatorEngine.calculateEnergyWh(v2, c2)
        assertEquals(0, BigDecimal("2400.0").compareTo(e2))
    }

    // 4. Watt load calculation
    @Test
    fun test4_wattLoadCalculation() {
        val voltage = BigDecimal("12.0")
        val loadWatts = BigDecimal("100")
        val resultWatts = CalculatorEngine.calculateLoadWatts(voltage, loadWatts, LoadMode.WATTS)
        assertEquals(0, BigDecimal("100").compareTo(resultWatts))
    }

    // 5. Amp load calculation
    @Test
    fun test5_ampLoadCalculation() {
        val voltage = BigDecimal("12.0")
        val currentAmps = BigDecimal("8.0")
        val loadWatts = CalculatorEngine.calculateLoadWatts(voltage, currentAmps, LoadMode.AMPS)
        // 12 * 8 = 96 W
        assertEquals(0, BigDecimal("96.0").compareTo(loadWatts))

        val v2 = BigDecimal("24.0")
        val a2 = BigDecimal("5.0")
        val w2 = CalculatorEngine.calculateLoadWatts(v2, a2, LoadMode.AMPS)
        // 24 * 5 = 120 W
        assertEquals(0, BigDecimal("120.0").compareTo(w2))
    }

    // 6. Efficiency calculation
    @Test
    fun test6_efficiencyCalculation() {
        val nominalWh = BigDecimal("1200")
        val usablePercent = BigDecimal("100")
        val efficiencyPercent = BigDecimal("90")
        val (usableWh, effectiveWh) = CalculatorEngine.calculateEffectiveEnergy(nominalWh, usablePercent, efficiencyPercent)
        assertEquals(0, BigDecimal("1200").compareTo(usableWh.stripTrailingZeros()))
        assertEquals(0, BigDecimal("1080").compareTo(effectiveWh.stripTrailingZeros()))
    }

    // 7. Usable capacity calculation
    @Test
    fun test7_usableCapacityCalculation() {
        val nominalWh = BigDecimal("1200")
        val usablePercent = BigDecimal("80")
        val efficiencyPercent = BigDecimal("90")
        val (usableWh, effectiveWh) = CalculatorEngine.calculateEffectiveEnergy(nominalWh, usablePercent, efficiencyPercent)
        // Usable = 1200 * 0.8 = 960 Wh
        assertEquals(0, BigDecimal("960").compareTo(usableWh.stripTrailingZeros()))
        // Effective = 960 * 0.9 = 864 Wh
        assertEquals(0, BigDecimal("864").compareTo(effectiveWh.stripTrailingZeros()))
    }

    // 8. Runtime calculation
    @Test
    fun test8_runtimeCalculation() {
        val effectiveWh = BigDecimal("1080")
        val loadWatts = BigDecimal("100")
        val runtimeHours = CalculatorEngine.calculateRuntimeHours(effectiveWh, loadWatts)
        // 1080 / 100 = 10.8 hours
        assertEquals(0, BigDecimal("10.8").compareTo(runtimeHours.stripTrailingZeros()))

        val runtime2 = CalculatorEngine.calculateRuntimeHours(effectiveWh, BigDecimal("120"))
        // 1080 / 120 = 9 hours
        assertEquals(0, BigDecimal("9").compareTo(runtime2.stripTrailingZeros()))
    }

    // 9. Runtime formatting
    @Test
    fun test9_runtimeFormatting() {
        // Less than 60 minutes
        assertEquals("45 min", Formatters.formatRuntime(BigDecimal("0.75")))

        // 1 hour (zero minutes hidden)
        assertEquals("1 h", Formatters.formatRuntime(BigDecimal("1.0")))

        // 2.5 hours
        assertEquals("2 h 30 min", Formatters.formatRuntime(BigDecimal("2.5")))

        // 24 hours (zero hours and zero minutes hidden)
        assertEquals("1 d", Formatters.formatRuntime(BigDecimal("24.0")))

        // 26.5 hours
        assertEquals("1 d 2 h 30 min", Formatters.formatRuntime(BigDecimal("26.5")))

        // 10.8 hours = 10 h 48 min
        assertEquals("10 h 48 min", Formatters.formatRuntime(BigDecimal("10.8")))

        // Zero hours
        assertEquals("0 min", Formatters.formatRuntime(BigDecimal("0")))

        // Tiny duration (< 1 min)
        assertEquals("< 1 min", Formatters.formatRuntime(BigDecimal("0.005")))
    }

    // 10. Zero load validation
    @Test
    fun test10_zeroLoadValidation() {
        val error = CalculatorEngine.validateLoad(BigDecimal.ZERO)
        assertEquals("Load must be greater than zero.", error)
    }

    // 11. Negative load validation
    @Test
    fun test11_negativeLoadValidation() {
        val error = CalculatorEngine.validateLoad(BigDecimal("-5"))
        assertEquals("Load must be greater than zero.", error)
    }

    // 12. Zero voltage validation
    @Test
    fun test12_zeroVoltageValidation() {
        val error = CalculatorEngine.validateVoltage(BigDecimal.ZERO)
        assertEquals("Enter a valid battery voltage.", error)
    }

    // 13. Negative capacity validation
    @Test
    fun test13_negativeCapacityValidation() {
        val error = CalculatorEngine.validateCapacity(BigDecimal("-10"))
        assertEquals("Enter a valid battery capacity.", error)
    }

    // 14. Efficiency validation
    @Test
    fun test14_efficiencyValidation() {
        val errorLow = CalculatorEngine.validateEfficiency(BigDecimal("49.9"))
        assertEquals("Efficiency must be between 50% and 100%.", errorLow)

        val errorHigh = CalculatorEngine.validateEfficiency(BigDecimal("100.1"))
        assertEquals("Efficiency must be between 50% and 100%.", errorHigh)

        val validMid = CalculatorEngine.validateEfficiency(BigDecimal("90"))
        assertNull(validMid)

        val validMin = CalculatorEngine.validateEfficiency(BigDecimal("50"))
        assertNull(validMin)

        val validMax = CalculatorEngine.validateEfficiency(BigDecimal("100"))
        assertNull(validMax)
    }

    // 15. Usable capacity validation
    @Test
    fun test15_usableCapacityValidation() {
        val errorLow = CalculatorEngine.validateUsableCapacity(BigDecimal("0.9"))
        assertEquals("Usable capacity must be between 1% and 100%.", errorLow)

        val errorHigh = CalculatorEngine.validateUsableCapacity(BigDecimal("100.1"))
        assertEquals("Usable capacity must be between 1% and 100%.", errorHigh)

        val valid100 = CalculatorEngine.validateUsableCapacity(BigDecimal("100"))
        assertNull(valid100)

        val valid1 = CalculatorEngine.validateUsableCapacity(BigDecimal("1"))
        assertNull(valid1)
    }

    // 16. Decimal calculations
    @Test
    fun test16_decimalCalculations() {
        val input = BatteryCalculationInput(
            voltage = BigDecimal("3.7"),
            capacity = BigDecimal("2500"),
            capacityUnit = CapacityUnit.MAH,
            loadValue = BigDecimal("1.85"),
            loadMode = LoadMode.WATTS,
            efficiencyPercent = BigDecimal("92"),
            usableCapacityPercent = BigDecimal("95")
        )
        val result = CalculatorEngine.calculate(input)

        // 2500 mAh = 2.5 Ah
        assertEquals(0, BigDecimal("2.5").compareTo(result.capacityAh.stripTrailingZeros()))

        // Nominal = 3.7 * 2.5 = 9.25 Wh
        assertEquals(0, BigDecimal("9.25").compareTo(result.nominalEnergyWh.stripTrailingZeros()))

        // Usable = 9.25 * 0.95 = 8.7875 Wh
        assertEquals(0, BigDecimal("8.7875").compareTo(result.usableEnergyWh.stripTrailingZeros()))

        // Effective = 8.7875 * 0.92 = 8.0845 Wh
        assertEquals(0, BigDecimal("8.0845").compareTo(result.effectiveEnergyWh.stripTrailingZeros()))

        // Runtime = 8.0845 / 1.85 = 4.37 hours
        val roundedRuntime = result.runtimeHours.setScale(2, RoundingMode.HALF_UP)
        assertEquals(0, BigDecimal("4.37").compareTo(roundedRuntime))
    }

    // 17. Large values
    @Test
    fun test17_largeValues() {
        val input = BatteryCalculationInput(
            voltage = BigDecimal("800.0"),
            capacity = BigDecimal("500.0"),
            capacityUnit = CapacityUnit.AH,
            loadValue = BigDecimal("50000.0"),
            loadMode = LoadMode.WATTS,
            efficiencyPercent = BigDecimal("95.0"),
            usableCapacityPercent = BigDecimal("90.0")
        )
        val result = CalculatorEngine.calculate(input)

        // Nominal = 800 * 500 = 400,000 Wh
        assertEquals(0, BigDecimal("400000.00").compareTo(result.nominalEnergyWh))

        // Usable = 400,000 * 0.90 = 360,000 Wh
        assertEquals(0, BigDecimal("360000.00").compareTo(result.usableEnergyWh.setScale(2, RoundingMode.HALF_UP)))

        // Effective = 360,000 * 0.95 = 342,000 Wh
        assertEquals(0, BigDecimal("342000.00").compareTo(result.effectiveEnergyWh.setScale(2, RoundingMode.HALF_UP)))

        // Runtime = 342,000 / 50,000 = 6.84 hours
        assertEquals(0, BigDecimal("6.84").compareTo(result.runtimeHours.setScale(2, RoundingMode.HALF_UP)))
    }

    // 18. History serialization
    @Test
    fun test18_historySerialization() {
        val item = CalculationHistoryItem(
            id = "test-123",
            timestamp = 1700000000000L,
            formattedDate = "Oct 02, 2026 · 14:00",
            voltageV = "12.0",
            capacity = "100",
            capacityUnit = CapacityUnit.AH,
            loadMode = LoadMode.WATTS,
            loadValue = "100",
            estimatedLoadWatts = "100 W",
            efficiencyPercent = "90",
            usableCapacityPercent = "100",
            nominalEnergyWh = "1,200 Wh",
            usableEnergyWh = "1,200 Wh",
            effectiveEnergyWh = "1,080 Wh",
            runtimeHours = "10.80 hours",
            runtimeFormatted = "10 h 48 min"
        )

        val array = JSONArray()
        val obj = JSONObject().apply {
            put("id", item.id)
            put("timestamp", item.timestamp)
            put("formattedDate", item.formattedDate)
            put("voltageV", item.voltageV)
            put("capacity", item.capacity)
            put("capacityUnit", item.capacityUnit.name)
            put("loadMode", item.loadMode.name)
            put("loadValue", item.loadValue)
            put("estimatedLoadWatts", item.estimatedLoadWatts)
            put("efficiencyPercent", item.efficiencyPercent)
            put("usableCapacityPercent", item.usableCapacityPercent)
            put("nominalEnergyWh", item.nominalEnergyWh)
            put("usableEnergyWh", item.usableEnergyWh)
            put("effectiveEnergyWh", item.effectiveEnergyWh)
            put("runtimeHours", item.runtimeHours)
            put("runtimeFormatted", item.runtimeFormatted)
        }
        array.put(obj)
        val serialized = array.toString()

        // Deserialize
        val deserializedArray = JSONArray(serialized)
        assertEquals(1, deserializedArray.length())
        val recovered = deserializedArray.getJSONObject(0)
        assertEquals("test-123", recovered.getString("id"))
        assertEquals("12.0", recovered.getString("voltageV"))
        assertEquals("100", recovered.getString("capacity"))
        assertEquals("AH", recovered.getString("capacityUnit"))
        assertEquals("WATTS", recovered.getString("loadMode"))
        assertEquals("10 h 48 min", recovered.getString("runtimeFormatted"))
    }

    // Prompt Known Example 1:
    // Battery: 12 V, 100 Ah, Load: 100 W, Efficiency: 90%, Usable: 100%
    // Nominal: 1200 Wh, Effective: 1080 Wh, Runtime: 10.8 hours, Display: 10 h 48 min
    @Test
    fun testKnownExample1() {
        val input = BatteryCalculationInput(
            voltage = BigDecimal("12.0"),
            capacity = BigDecimal("100"),
            capacityUnit = CapacityUnit.AH,
            loadValue = BigDecimal("100"),
            loadMode = LoadMode.WATTS,
            efficiencyPercent = BigDecimal("90"),
            usableCapacityPercent = BigDecimal("100")
        )
        val result = CalculatorEngine.calculate(input)

        assertEquals(0, BigDecimal("1200.0").compareTo(result.nominalEnergyWh))
        assertEquals(0, BigDecimal("1080.0").compareTo(result.effectiveEnergyWh.setScale(1, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("10.8").compareTo(result.runtimeHours.setScale(1, RoundingMode.HALF_UP)))
        assertEquals("10 h 48 min", Formatters.formatRuntime(result.runtimeHours))
    }

    // Prompt Known Example 2:
    // Battery: 24 V, 50 Ah, Load: 5 A, Efficiency: 90%, Usable: 100%
    // Load power: 120 W, Nominal: 1200 Wh, Effective: 1080 Wh, Runtime: 9 hours
    @Test
    fun testKnownExample2() {
        val input = BatteryCalculationInput(
            voltage = BigDecimal("24.0"),
            capacity = BigDecimal("50"),
            capacityUnit = CapacityUnit.AH,
            loadValue = BigDecimal("5.0"),
            loadMode = LoadMode.AMPS,
            efficiencyPercent = BigDecimal("90"),
            usableCapacityPercent = BigDecimal("100")
        )
        val result = CalculatorEngine.calculate(input)

        assertEquals(0, BigDecimal("120.0").compareTo(result.loadWatts))
        assertEquals(0, BigDecimal("1200.0").compareTo(result.nominalEnergyWh))
        assertEquals(0, BigDecimal("1080.0").compareTo(result.effectiveEnergyWh.setScale(1, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("9.0").compareTo(result.runtimeHours.setScale(1, RoundingMode.HALF_UP)))
        assertEquals("9 h", Formatters.formatRuntime(result.runtimeHours))
    }
}
