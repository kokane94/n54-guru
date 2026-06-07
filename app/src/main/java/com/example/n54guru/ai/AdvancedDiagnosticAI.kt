package com.example.n54guru.ai

import com.example.n54guru.models.*

class AdvancedDiagnosticAI {
    fun analyzeAllSystems(
        engineParams: Map<String, OBDParameter>,
        electricalParams: Map<String, ElectricalParameter>,
        historicalData: List<DiagnosticLog>
    ): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val diagnosticAI = DiagnosticAI()
        
        alerts.addAll(diagnosticAI.analyzeDiagnosticData(engineParams, historicalData))
        alerts.addAll(analyzeBattery(electricalParams))
        alerts.addAll(analyzeHVAC(electricalParams))
        alerts.addAll(analyzeRoof(electricalParams))
        alerts.addAll(analyzeSafety(electricalParams))
        
        return alerts.sortedByDescending { it.severity.ordinal }
    }

    private fun analyzeBattery(electrical: Map<String, ElectricalParameter>): List<DiagnosticAlert> {
        val alerts = mutableListOf<DiagnosticAlert>()
        val voltage = electrical["0101"]?.value ?: return alerts

        if (voltage < 12f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = "Battery",
                predictedFailureTime = 24 * 60 * 60 * 1000,
                reason = "Battery voltage critical: ${voltage.toInt()}V (normal: 13.8V)",
                recommendation = "Charge or replace battery immediately. May fail to start vehicle.",
                confidence = 95f
            ))
        } else if (voltage > 15f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Alternator/Charging System",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000,
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
                predictedFailureTime = 14 * 24 * 60 * 60 * 1000,
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
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000,
                reason = "Roof motor stalling: ${motorCurrent.toInt()}A draw. Roof may be binding.",
                recommendation = "Check roof tracks for debris. May need lubrication or replacement.",
                confidence = 75f
            ))
        }

        if (roofLatch == 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = "Roof Latch Mechanism",
                predictedFailureTime = 0,
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
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000,
                reason = "Alarm system offline",
                recommendation = "Security system not armed. Check alarm module.",
                confidence = 80f
            ))
        }

        if (camera == 0f) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.WARNING,
                component = "Backup Camera",
                predictedFailureTime = 7 * 24 * 60 * 60 * 1000,
                reason = "Backup camera offline",
                recommendation = "Camera not transmitting. Check wiring and camera unit.",
                confidence = 85f
            ))
        }
        return alerts
    }
}
