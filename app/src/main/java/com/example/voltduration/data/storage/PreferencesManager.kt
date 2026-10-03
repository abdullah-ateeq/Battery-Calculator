package com.example.voltduration.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.voltduration.data.model.AppSettings
import com.example.voltduration.data.model.CalculationHistoryItem
import com.example.voltduration.data.model.CapacityUnit
import com.example.voltduration.data.model.LoadMode
import com.example.voltduration.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voltduration_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private const val MAX_HISTORY_ITEMS = 50

        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_VOLTAGE = stringPreferencesKey("default_voltage")
        private val KEY_DEFAULT_CAPACITY = stringPreferencesKey("default_capacity")
        private val KEY_DEFAULT_CAPACITY_UNIT = stringPreferencesKey("default_capacity_unit")
        private val KEY_DEFAULT_LOAD_MODE = stringPreferencesKey("default_load_mode")
        private val KEY_DEFAULT_LOAD_VALUE = stringPreferencesKey("default_load_value")
        private val KEY_DEFAULT_EFFICIENCY = stringPreferencesKey("default_efficiency")
        private val KEY_DEFAULT_USABLE_CAPACITY = stringPreferencesKey("default_usable_capacity")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_SHOW_ENERGY_BREAKDOWN = booleanPreferencesKey("show_energy_breakdown")
        private val KEY_SHOW_LIMITATIONS = booleanPreferencesKey("show_limitations")
        private val KEY_HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true
        val defaultVoltage = prefs[KEY_DEFAULT_VOLTAGE] ?: "12"
        val defaultCapacity = prefs[KEY_DEFAULT_CAPACITY] ?: "100"
        val capUnitStr = prefs[KEY_DEFAULT_CAPACITY_UNIT] ?: CapacityUnit.AH.name
        val defaultCapUnit = try {
            CapacityUnit.valueOf(capUnitStr)
        } catch (_: Exception) {
            CapacityUnit.AH
        }

        val loadModeStr = prefs[KEY_DEFAULT_LOAD_MODE] ?: LoadMode.WATTS.name
        val defaultLoadMode = try {
            LoadMode.valueOf(loadModeStr)
        } catch (_: Exception) {
            LoadMode.WATTS
        }

        val defaultLoadValue = prefs[KEY_DEFAULT_LOAD_VALUE] ?: "100"
        val defaultEfficiency = prefs[KEY_DEFAULT_EFFICIENCY] ?: "90"
        val defaultUsableCapacity = prefs[KEY_DEFAULT_USABLE_CAPACITY] ?: "100"
        val hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true
        val showEnergyBreakdown = prefs[KEY_SHOW_ENERGY_BREAKDOWN] ?: true
        val showLimitations = prefs[KEY_SHOW_LIMITATIONS] ?: true

        AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            defaultVoltage = defaultVoltage,
            defaultCapacity = defaultCapacity,
            defaultCapacityUnit = defaultCapUnit,
            defaultLoadMode = defaultLoadMode,
            defaultLoadValue = defaultLoadValue,
            defaultEfficiency = defaultEfficiency,
            defaultUsableCapacity = defaultUsableCapacity,
            hapticFeedback = hapticFeedback,
            showEnergyBreakdown = showEnergyBreakdown,
            showLimitations = showLimitations
        )
    }

    val historyFlow: Flow<List<CalculationHistoryItem>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
        deserializeHistory(jsonString)
    }

    suspend fun saveCalculation(item: CalculationHistoryItem) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = deserializeHistory(jsonString).toMutableList()

            // Prepend new item (newest first)
            currentList.add(0, item)

            // Keep max 50 records. If > 50, trim oldest from the end
            val trimmedList = if (currentList.size > MAX_HISTORY_ITEMS) {
                currentList.take(MAX_HISTORY_ITEMS)
            } else {
                currentList
            }

            prefs[KEY_HISTORY_JSON] = serializeHistory(trimmedList)
        }
    }

    suspend fun deleteCalculation(itemId: String) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = deserializeHistory(jsonString).filter { it.id != itemId }
            prefs[KEY_HISTORY_JSON] = serializeHistory(currentList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HISTORY_JSON] = "[]"
        }
    }

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val currentSettings = AppSettings(
                themeMode = try {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                },
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
                defaultVoltage = prefs[KEY_DEFAULT_VOLTAGE] ?: "12",
                defaultCapacity = prefs[KEY_DEFAULT_CAPACITY] ?: "100",
                defaultCapacityUnit = try {
                    CapacityUnit.valueOf(prefs[KEY_DEFAULT_CAPACITY_UNIT] ?: CapacityUnit.AH.name)
                } catch (_: Exception) {
                    CapacityUnit.AH
                },
                defaultLoadMode = try {
                    LoadMode.valueOf(prefs[KEY_DEFAULT_LOAD_MODE] ?: LoadMode.WATTS.name)
                } catch (_: Exception) {
                    LoadMode.WATTS
                },
                defaultLoadValue = prefs[KEY_DEFAULT_LOAD_VALUE] ?: "100",
                defaultEfficiency = prefs[KEY_DEFAULT_EFFICIENCY] ?: "90",
                defaultUsableCapacity = prefs[KEY_DEFAULT_USABLE_CAPACITY] ?: "100",
                hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
                showEnergyBreakdown = prefs[KEY_SHOW_ENERGY_BREAKDOWN] ?: true,
                showLimitations = prefs[KEY_SHOW_LIMITATIONS] ?: true
            )

            val newSettings = update(currentSettings)
            prefs[KEY_THEME_MODE] = newSettings.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = newSettings.dynamicColor
            prefs[KEY_DEFAULT_VOLTAGE] = newSettings.defaultVoltage
            prefs[KEY_DEFAULT_CAPACITY] = newSettings.defaultCapacity
            prefs[KEY_DEFAULT_CAPACITY_UNIT] = newSettings.defaultCapacityUnit.name
            prefs[KEY_DEFAULT_LOAD_MODE] = newSettings.defaultLoadMode.name
            prefs[KEY_DEFAULT_LOAD_VALUE] = newSettings.defaultLoadValue
            prefs[KEY_DEFAULT_EFFICIENCY] = newSettings.defaultEfficiency
            prefs[KEY_DEFAULT_USABLE_CAPACITY] = newSettings.defaultUsableCapacity
            prefs[KEY_HAPTIC_FEEDBACK] = newSettings.hapticFeedback
            prefs[KEY_SHOW_ENERGY_BREAKDOWN] = newSettings.showEnergyBreakdown
            prefs[KEY_SHOW_LIMITATIONS] = newSettings.showLimitations
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            val defaultSettings = AppSettings()
            prefs[KEY_THEME_MODE] = defaultSettings.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = defaultSettings.dynamicColor
            prefs[KEY_DEFAULT_VOLTAGE] = defaultSettings.defaultVoltage
            prefs[KEY_DEFAULT_CAPACITY] = defaultSettings.defaultCapacity
            prefs[KEY_DEFAULT_CAPACITY_UNIT] = defaultSettings.defaultCapacityUnit.name
            prefs[KEY_DEFAULT_LOAD_MODE] = defaultSettings.defaultLoadMode.name
            prefs[KEY_DEFAULT_LOAD_VALUE] = defaultSettings.defaultLoadValue
            prefs[KEY_DEFAULT_EFFICIENCY] = defaultSettings.defaultEfficiency
            prefs[KEY_DEFAULT_USABLE_CAPACITY] = defaultSettings.defaultUsableCapacity
            prefs[KEY_HAPTIC_FEEDBACK] = defaultSettings.hapticFeedback
            prefs[KEY_SHOW_ENERGY_BREAKDOWN] = defaultSettings.showEnergyBreakdown
            prefs[KEY_SHOW_LIMITATIONS] = defaultSettings.showLimitations
        }
    }

    fun serializeHistory(items: List<CalculationHistoryItem>): String {
        val array = JSONArray()
        for (item in items) {
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
        }
        return array.toString()
    }

    fun deserializeHistory(jsonString: String): List<CalculationHistoryItem> {
        val list = mutableListOf<CalculationHistoryItem>()
        if (jsonString.isBlank()) return list
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val item = CalculationHistoryItem(
                    id = obj.optString("id", "${obj.optLong("timestamp", 0)}-$i"),
                    timestamp = obj.optLong("timestamp", 0L),
                    formattedDate = obj.optString("formattedDate", ""),
                    voltageV = obj.optString("voltageV", "12"),
                    capacity = obj.optString("capacity", "100"),
                    capacityUnit = try {
                        CapacityUnit.valueOf(obj.optString("capacityUnit", "AH"))
                    } catch (_: Exception) {
                        CapacityUnit.AH
                    },
                    loadMode = try {
                        LoadMode.valueOf(obj.optString("loadMode", "WATTS"))
                    } catch (_: Exception) {
                        LoadMode.WATTS
                    },
                    loadValue = obj.optString("loadValue", "100"),
                    estimatedLoadWatts = obj.optString("estimatedLoadWatts", "100"),
                    efficiencyPercent = obj.optString("efficiencyPercent", "90"),
                    usableCapacityPercent = obj.optString("usableCapacityPercent", "100"),
                    nominalEnergyWh = obj.optString("nominalEnergyWh", "1200"),
                    usableEnergyWh = obj.optString("usableEnergyWh", "1200"),
                    effectiveEnergyWh = obj.optString("effectiveEnergyWh", "1080"),
                    runtimeHours = obj.optString("runtimeHours", "10.8"),
                    runtimeFormatted = obj.optString("runtimeFormatted", "10 h 48 min")
                )
                list.add(item)
            }
        } catch (_: Exception) {
            // Safe fallback on parse error
        }
        return list
    }
}
