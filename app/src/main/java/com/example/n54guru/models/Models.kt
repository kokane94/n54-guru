package com.example.n54guru.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertSeverity { INFO, WARNING, CRITICAL }

enum class VoiceAccent {
    HORI_MAORI_NZ,
    PROUD_AUSSIE,
    AUSSIE_LEBO_WOG,
    SCOTTISH_BLOKE,
    BRITISH_POSH,
    IRISH_CHARM,
    SOUTH_AFRICAN_BOER,
    AMERICAN_SOUTHERN,
    AMERICAN_NEW_YORK,
    CANADIAN_MAPLE,
    KIWI_BLOKE,
    GERMAN_BOSS
}

data class OBDParameter(
    val pid: String,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ElectricalParameter(
    val pid: String,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "obd_logs")
data class OBDLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pid: String,
    val value: Float,
    val unit: String,
    val timestamp: Long
)

typealias DiagnosticLog = OBDLog

data class PartSearchResult(
    val name: String,
    val price: String,
    val link: String,
    val source: String
)

data class DiagnosticAlert(
    val severity: AlertSeverity,
    val component: String,
    val predictedFailureTime: Long,
    val reason: String,
    val recommendation: String,
    val confidence: Float = 85f
)
