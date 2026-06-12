package com.example.n54guru.ai

import com.example.n54guru.models.*
import com.example.n54guru.ai.DiagnosticAI

class AdvancedDiagnosticAI {
    fun analyzeAllSystems(
        engineParams: Map<String, OBDParameter>,
        electricalParams: Map<String, ElectricalParameter>,
        historicalData: List<DiagnosticLog>
    ): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val diagnosticAI = DiagnosticAI()
        
        alerts.addAll(diagnosticAI.analyzeDiagnosticData(engineParams, historicalData.map { OBDParameter(it.pid, it.value, it.unit, it.timestamp) }))
        alerts.addAll(analyzeHPFP(engineParams, historicalData))
        alerts.addAll(analyzeOilTemperature(engineParams, historicalData))
        alerts.addAll(analyzeMisfires(engineParams, historicalData))
        alerts.addAll(analyzeBattery(electricalParams))
        alerts.addAll(analyzeHVAC(electricalParams))
        alerts.addAll(analyzeRoof(electricalParams))
        alerts.addAll(analyzeSafety(electricalParams))
        
        return alerts.sortedByDescending { it.severity.ordinal }
    }

    private fun analyzeHPFP(engineParams: Map<String, OBDParameter>, historicalData: List<DiagnosticLog>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val hpfpPressure = engineParams["HPFPPressure"]?.value ?: return alerts

        // N54 HPFP typically operates around 70-100 bar (1000-1450 psi) at idle/cruise
        // Low pressure can indicate a failing HPFP
        if (hpfpPressure < 500) { // Example threshold in kPa (approx 72 psi)
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.CRITICAL,
                    component = "High Pressure Fuel Pump (HPFP)",
                    predictedFailureTime = 7 * 24 * 60 * 60 * 1000L, // 7 days
                    reason = "HPFP pressure critically low: ${hpfpPressure.toInt()} kPa. Engine may go into limp mode or stall.",
                    recommendation = "Inspect HPFP and fuel system. Prepare for HPFP replacement.",
                    confidence = 95f
                )
            )
        } else if (hpfpPressure < 700) { // Example threshold in kPa (approx 100 psi)
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.WARNING,
                    component = "High Pressure Fuel Pump (HPFP)",
                    predictedFailureTime = 30 * 24 * 60 * 60 * 1000L, // 30 days
                    reason = "HPFP pressure low: ${hpfpPressure.toInt()} kPa. May indicate early stages of failure.",
                    recommendation = "Monitor HPFP pressure closely. Consider preventative maintenance.",
                    confidence = 70f
                )
            )
        }
        return alerts
    }

    private fun analyzeOilTemperature(engineParams: Map<String, OBDParameter>, historicalData: List<DiagnosticLog>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val oilTemp = engineParams["OilTemp"]?.value ?: return alerts

        // N54 oil temperature typically runs around 100-120°C (212-248°F)
        if (oilTemp > 130f) {
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.CRITICAL,
                    component = "Engine Oil System",
                    predictedFailureTime = 0L, // Immediate attention
                    reason = "Engine oil temperature critically high: ${oilTemp.toInt()}°C. Risk of engine damage.",
                    recommendation = "Stop driving immediately. Check oil level and cooling system.",
                    confidence = 98f
                )
            )
        } else if (oilTemp > 120f) {
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.WARNING,
                    component = "Engine Oil System",
                    predictedFailureTime = 1 * 24 * 60 * 60 * 1000L, // 1 day
                    reason = "Engine oil temperature high: ${oilTemp.toInt()}°C. Monitor closely.",
                    recommendation = "Reduce engine load. Check oil cooler and fluid levels.",
                    confidence = 85f
                )
            )
        }
        return alerts
    }

    private fun analyzeMisfires(engineParams: Map<String, OBDParameter>, historicalData: List<DiagnosticLog>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val misfireCyl1 = engineParams["MisfireCyl1"]?.value ?: 0f // Assuming PID 01XX for misfire cylinder 1

        if (misfireCyl1 > 5) { // More than 5 misfires on a cylinder is usually a concern
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.CRITICAL,
                    component = "Ignition/Fuel System (Cylinder 1)",
                    predictedFailureTime = 0L, // Immediate attention
                    reason = "Severe misfires detected on Cylinder 1: ${misfireCyl1.toInt()} counts. Affects engine performance and can damage catalytic converter.",
                    recommendation = "Inspect spark plug, ignition coil, and fuel injector for Cylinder 1. Avoid heavy acceleration.",
                    confidence = 99f
                )
            )
        } else if (misfireCyl1 > 0) {
            alerts.add(
                DiagnosticAlert(
                    severity = AlertSeverity.WARNING,
                    component = "Ignition/Fuel System (Cylinder 1)",
                    predictedFailureTime = 7 * 24 * 60 * 60 * 1000L, // 7 days
                    reason = "Misfires detected on Cylinder 1: ${misfireCyl1.toInt()} counts. May indicate early stage issue.",
                    recommendation = "Monitor misfire counts. Consider checking spark plugs or coils during next service.",
                    confidence = 75f
                )
            )
        }
        // Extend for other cylinders as needed (MisfireCyl2, MisfireCyl3, etc.)
        return alerts
    }

    private fun analyzeBattery(electrical: Map<String, ElectricalParameter>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val voltage = electrical["0101"]?.value ?: return alerts

        if (voltage < 12f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = "Battery",
                predictedFailureTime = 24 * 60 * 60 * 1000L,
                reason = "Battery voltage critical: ${voltage.toInt()}V (normal: 13.8V)",
                recommendation = "Charge or replace battery immediately. May fail to start vehicle.",
                confidence = 95f
            ))
        } else if (voltage > 15f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Alternator/Charging System",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000L,
                reason = "Battery overcharging: ${voltage.toInt()}V (normal: 13.8V)",
                recommendation = "Alternator may be faulty. Have charging system tested.",
                confidence = 80f
            ))
        }
        return alerts
    }

    private fun analyzeHVAC(electrical: Map<String, ElectricalParameter>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val acPressure = electrical["0301"]?.value ?: return alerts

        if (acPressure < 50f && acPressure > 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "AC Compressor",
                predictedFailureTime = 14 * 24 * 60 * 60 * 1000L,
                reason = "AC pressure low: ${acPressure.toInt()} psi. Possible refrigerant leak.",
                recommendation = "Get AC system checked for leaks. May need refrigerant recharge.",
                confidence = 70f
            ))
        }
        return alerts
    }

    private fun analyzeRoof(electrical: Map<String, ElectricalParameter>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val motorCurrent = electrical["0502"]?.value ?: return alerts
        val roofLatch = electrical["0504"]?.value ?: 1f

        if (motorCurrent > 40f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Convertible Roof Motor",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000L,
                reason = "Roof motor stalling: ${motorCurrent.toInt()}A draw. Roof may be binding.",
                recommendation = "Check roof tracks for debris. May need lubrication or replacement.",
                confidence = 75f
            ))
        }

        if (roofLatch == 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = "Roof Latch Mechanism",
                predictedFailureTime = 0L,
                reason = "Roof latch disengaged or failed",
                recommendation = "Roof may open unexpectedly. Get latch mechanism inspected.",
                confidence = 95f
            ))
        }
        return alerts
    }

    private fun analyzeSafety(electrical: Map<String, ElectricalParameter>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val alarm = electrical["0703"]?.value ?: 1f
        val camera = electrical["0704"]?.value ?: 1f

        if (alarm == 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Security System",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000L,
                reason = "Alarm system offline",
                recommendation = "Security system not armed. Check alarm module.",
                confidence = 80f
            ))
        }

        if (camera == 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Backup Camera",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000L,
                reason = "Backup camera offline",
                recommendation = "Camera not transmitting. Check wiring and camera unit.",
                confidence = 85f
            ))
        }
        return alerts
    }
}
