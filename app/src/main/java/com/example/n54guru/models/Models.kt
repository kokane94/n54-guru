package com.example.n54guru.models

enum class AlertSeverity { INFO, WARNING, CRITICAL }

data class OBDParameter(
    val pid: String, 
    val value: Float, 
    val unit: String, 
    val timestamp: Long = System.currentTimeMillis()
)

data class DiagnosticAlert(
    val severity: AlertSeverity,
    val component: String,
    val predictedFailureTime: Long,
    val reason: String,
    val recommendation: String,
    val confidence: Float = 85f
)
