package com.example.n54guru.models

/**
 * Canonical N54 PID → parameter definitions.
 *
 * Sources: public SAE J1979 (OBD-II PIDs) and BMW N54-specific DME PIDs
 * documented in the public technical literature. Keep this the single
 * source of truth for what we poll.
 */
object N54Parameters {
    data class ParameterDef(
        val pid: String,
        val name: String,
        val unit: String,
        val minSafe: Float = Float.NaN,
        val maxSafe: Float = Float.NaN
    )

    val ENGINE_PARAMETERS: Map<String, ParameterDef> = mapOf(
        "010C" to ParameterDef("010C", "Engine RPM",       "rpm",   700f,  7000f),
        "010D" to ParameterDef("010D", "Vehicle Speed",    "km/h",  0f,    250f),
        "0105" to ParameterDef("0105", "Coolant Temp",     "°C",    70f,   105f),
        "010B" to ParameterDef("010B", "Intake MAP",       "kPa",   20f,   250f),
        "010F" to ParameterDef("010F", "Intake Air Temp",  "°C",    5f,    60f),
        "0110" to ParameterDef("0110", "MAF",              "g/s",   0f,    600f),
        "0111" to ParameterDef("0111", "Throttle Position","%",     0f,    100f),
        "011F" to ParameterDef("011F", "Run Time",         "sec",   0f,    Float.MAX_VALUE),
        "012F" to ParameterDef("012F", "Fuel Level",       "%",     0f,    100f),
        "0142" to ParameterDef("0142", "Control Module Voltage", "V", 12f, 15f),
        "0146" to ParameterDef("0146", "Ambient Air Temp", "°C",    -20f,  50f),
        "015C" to ParameterDef("015C", "Engine Oil Temp",  "°C",    80f,   130f),
        "012E" to ParameterDef("012E", "Fuel Pressure (HPFP)", "kPa", 50f, 200f)
    )

    val TRANSMISSION_PARAMETERS: Map<String, ParameterDef> = mapOf(
        // ZF 6HP19 / GM 6L45E PIDs — left as a starting point; E93 uses
        // GM 6L45E which speaks different PIDs than the manual.
        "01A6" to ParameterDef("01A6", "Trans Fluid Temp", "°C", 60f, 130f)
    )

    val ALL: Map<String, ParameterDef> = ENGINE_PARAMETERS + TRANSMISSION_PARAMETERS
}
