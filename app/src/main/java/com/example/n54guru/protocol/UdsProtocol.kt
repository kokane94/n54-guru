package com.example.n54guru.protocol

/**
 * UDS (Unified Diagnostic Services) protocol constants.
 *
 * Based on ISO 14229 — Road vehicles — Unified diagnostic services (UDS).
 * This is an open international standard; no proprietary or vendor-specific
 * content is bundled here. Reference impl patterns taken from
 * kokane94/python-udsoncan, reimplemented in Kotlin.
 *
 * UDS uses a request/response pattern over CAN. Request is sent to the
 * module's diagnostic request ID. Response comes back on the matching
 * response ID = request_id + 0x40 (or a configured response ID).
 *
 * CAN frame format for diagnostics (ISO 15765 / ISO-TP) is implemented
 * separately in [IsotpTransport].
 */
object UdsProtocol {

    // ---------- Service IDs (SID) ----------
    object ServiceId {
        const val DIAGNOSTIC_SESSION_CONTROL = 0x10.toByte()
        const val ECU_RESET = 0x11.toByte()
        const val READ_DTC_INFORMATION = 0x19.toByte()
        const val READ_DATA_BY_IDENTIFIER = 0x22.toByte()
        const val READ_MEMORY_BY_ADDRESS = 0x23.toByte()
        const val READ_SCALING_DATA_BY_IDENTIFIER = 0x24.toByte()
        const val SECURITY_ACCESS = 0x27.toByte()
        const val COMMUNICATION_CONTROL = 0x28.toByte()
        const val READ_DATA_BY_PERIODIC_IDENTIFIER = 0x2A.toByte()
        const val DYNAMICALLY_DEFINE_DATA_IDENTIFIER = 0x2C.toByte()
        const val WRITE_DATA_BY_IDENTIFIER = 0x2E.toByte()
        const val INPUT_OUTPUT_CONTROL_BY_IDENTIFIER = 0x2F.toByte()
        const val ROUTINE_CONTROL = 0x31.toByte()
        const val REQUEST_DOWNLOAD = 0x34.toByte()
        const val REQUEST_UPLOAD = 0x35.toByte()
        const val TRANSFER_DATA = 0x36.toByte()
        const val REQUEST_TRANSFER_EXIT = 0x37.toByte()
        const val TESTER_PRESENT = 0x3E.toByte()
        const val NEGATIVE_RESPONSE = 0x7F.toByte()
    }

    // ---------- Positive response offset ----------
    // Response SID = Request SID + 0x40
    const val POSITIVE_RESPONSE_OFFSET: Byte = 0x40

    // ---------- Negative Response Codes (NRC) ----------
    // ISO 14229-1 Table 30
    object Nrc {
        const val GENERAL_REJECT: Byte = 0x10
        const val SERVICE_NOT_SUPPORTED: Byte = 0x11
        const val SUB_FUNCTION_NOT_SUPPORTED: Byte = 0x12
        const val INCORRECT_MESSAGE_LENGTH: Byte = 0x13
        const val CONDITIONS_NOT_CORRECT: Byte = 0x22
        const val REQUEST_OUT_OF_RANGE: Byte = 0x31
        const val SECURITY_ACCESS_DENIED: Byte = 0x33
        const val INVALID_KEY: Byte = 0x35
        const val EXCEEDED_NUMBER_OF_ATTEMPTS: Byte = 0x36
        const val REQUIRED_TIME_DELAY_NOT_EXPIRED: Byte = 0x37
        const val UPLOAD_DOWNLOAD_NOT_ACCEPTED: Byte = 0x70
        const val TRANSFER_DATA_SUSPENDED: Byte = 0x71
        const val GENERAL_PROGRAMMING_FAILURE: Byte = 0x72
        const val WRONG_BLOCK_SEQUENCE_COUNTER: Byte = 0x73
        const val REQUEST_CORRECTLY_RECEIVED_RESPONSE_PENDING: Byte = 0x78
        const val SUB_FUNCTION_NOT_SUPPORTED_IN_ACTIVE_SESSION: Byte = 0x7E
        const val SERVICE_NOT_SUPPORTED_IN_ACTIVE_SESSION: Byte = 0x7F
    }

    // ---------- Diagnostic Session Types (DST) ----------
    object Session {
        const val DEFAULT: Byte = 0x01
        const val PROGRAMMING: Byte = 0x02
        const val EXTENDED: Byte = 0x03
    }

    // ---------- Security Access Levels ----------
    object SecurityLevel {
        const val REQUEST_SEED: Byte = 0x01
        const val SEND_KEY: Byte = 0x02
        const val REQUEST_SEED_PROGRAMMING: Byte = 0x11
        const val SEND_KEY_PROGRAMMING: Byte = 0x12
    }

    // ---------- ReadDTCInformation sub-functions ----------
    object ReadDtcSubfunction {
        const val REPORT_NUMBER_OF_DTC_BY_STATUS_MASK: Byte = 0x01
        const val REPORT_DTC_BY_STATUS_MASK: Byte = 0x02
        const val REPORT_DTC_SNAPSHOT_IDENTIFICATION: Byte = 0x03
        const val REPORT_DTC_SNAPSHOT_RECORD_BY_DTC_NUMBER: Byte = 0x04
        const val REPORT_DTC_STORED_DATA_BY_RECORD_NUMBER: Byte = 0x05
        const val REPORT_DTC_EXT_DATA_RECORD_BY_DTC_NUMBER: Byte = 0x06
        const val REPORT_NUMBER_OF_DTC_BY_SEVERITY_MASK: Byte = 0x07
        const val REPORT_DTC_BY_SEVERITY_MASK: Byte = 0x08
        const val REPORT_SEVERITY_INFORMATION_OF_DTC: Byte = 0x09
        const val REPORT_SUPPORTED_DTC: Byte = 0x0A
        const val REPORT_FIRST_TEST_FAILED_DTC: Byte = 0x0B
        const val REPORT_FIRST_CONFIRMED_DTC: Byte = 0x0C
        const val REPORT_MOST_RECENT_TEST_FAILED_DTC: Byte = 0x0D
        const val REPORT_MOST_RECENT_CONFIRMED_DTC: Byte = 0x0E
        const val REPORT_DTC_WITH_PERMANENT_STATUS: Byte = 0x15
    }

    // ---------- DTC Status Bits ----------
    object DtcStatus {
        const val TEST_FAILED: Byte = 0x01
        const val TEST_FAILED_THIS_OP_CYCLE: Byte = 0x02
        const val PENDING_DTC: Byte = 0x04
        const val CONFIRMED_DTC: Byte = 0x08
        const val TEST_NOT_COMPLETED_SINCE_LAST_CLEAR: Byte = 0x10
        const val TEST_FAILED_SINCE_LAST_CLEAR: Byte = 0x20
        const val TEST_NOT_COMPLETED_THIS_OP_CYCLE: Byte = 0x40
        const val WARNING_INDICATOR_REQUESTED: Byte = 0x80.toByte()
    }
}

/**
 * Common DIDs (Data Identifiers) used in UDS ReadDataByIdentifier /
 * WriteDataByIdentifier (services 0x22 / 0x2E).
 *
 * These are DID addresses defined in the public spec and in BMW technical
 * documentation. Values are placeholders until the real DID table is
 * validated against the user's specific ECU variant.
 */
object CommonDids {
    // Vehicle identification
    const val VIN: Int = 0xF190
    const val ECU_SERIAL_NUMBER: Int = 0xF18C
    const val SYSTEM_SUPPLIER_IDENTIFIER: Int = 0xF18A
    const val ECU_MANUFACTURING_DATE: Int = 0xF18B
    const val SYSTEM_NAME: Int = 0xF197

    // BMW-specific (public, in BMW technical documentation)
    const val BMW_PART_NUMBER: Int = 0xF100
    const val BMW_HARDWARE_VERSION: Int = 0xF101
    const val BMW_SOFTWARE_VERSION: Int = 0xF102
    const val BMW_CALIBRATION_VERSION: Int = 0xF103
    const val BMW_REPAIR_SHOP_CODE: Int = 0xF104
    const val BMW_PROGRAMMING_DATE: Int = 0xF105
}

/**
 * CAN arbitration IDs for BMW E-series modules (E60/E90/E93 family).
 *
 * Sourced from kokane94/node-bmw-ref, which is a public reverse-engineering
 * project. The diagnostic request ID is the value the tester transmits TO.
 * The response ID is typically request_id + 0x40 (or a configured value).
 *
 * K-CAN = body CAN bus (low-speed, 100 kbps) — used for diagnostic on
 * E-series comfort modules.
 *
 * D-CAN = powertrain CAN bus (high-speed, 500 kbps) — used for DME,
 * EGS, DSC. This is where the N54 DME diagnostic requests go.
 */
object BmwCanIds {
    object Dme {
        // E60/E90/E93 N54 DME on PT-CAN
        const val DIAG_REQUEST: Int = 0x612  // Tester -> DME
        const val DIAG_RESPONSE: Int = 0x612 + 0x40  // = 0x652
    }

    object Egs {
        // E60/E90/E93 automatic transmission (E93 = GM 6L45E)
        const val DIAG_REQUEST: Int = 0x618
        const val DIAG_RESPONSE: Int = 0x658
    }

    object Dsc {
        // Dynamic Stability Control
        const val DIAG_REQUEST: Int = 0x6A2
        const val DIAG_RESPONSE: Int = 0x6A2 + 0x40
    }

    object Cas {
        // Car Access System (immobilizer / key recognition)
        const val DIAG_REQUEST: Int = 0x640
        const val DIAG_RESPONSE: Int = 0x680
    }

    object Kombi {
        // Instrument cluster
        const val DIAG_REQUEST: Int = 0x690
        const val DIAG_RESPONSE: Int = 0x6D0
    }

    object Ista {
        // Generic BMW diagnostic tester broadcast
        const val FUNCTIONAL_REQUEST: Int = 0x7DF  // OBD-II broadcast
        const val FUNCTIONAL_RESPONSE: Int = 0x7E8  // ECM
    }
}

/**
 * Standard OBD-II PIDs (SAE J1979) — these work on any OBD-II compliant
 * vehicle, including the N54 over D-CAN. Used for generic live data that
 * doesn't need UDS.
 */
object Obd2Pids {
    object Mode {
        const val LIVE_DATA: Byte = 0x01
        const val FREEZE_FRAME: Byte = 0x02
        const val READ_DTC: Byte = 0x03
        const val CLEAR_DTC: Byte = 0x04
        const val O2_TEST: Byte = 0x05
        const val PENDING_DTC: Byte = 0x07
        const val VEHICLE_INFO: Byte = 0x09
    }

    object Pid {
        const val SUPPORTED_PIDS_0_20: Byte = 0x00
        const val DTC_COUNT: Byte = 0x01
        const val FUEL_SYSTEM_STATUS: Byte = 0x03
        const val ENGINE_RPM: Byte = 0x0C        // (256*A + B) / 4
        const val VEHICLE_SPEED: Byte = 0x0D
        const val INTAKE_AIR_TEMP: Byte = 0x0F    // A - 40
        const val MAF_RATE: Byte = 0x10           // (256*A + B) / 100
        const val THROTTLE_POSITION: Byte = 0x11  // A * 100 / 255
        const val ENGINE_COOLANT_TEMP: Byte = 0x05  // A - 40
        const val SHORT_FUEL_TRIM_1: Byte = 0x06   // (A - 128) * 100 / 128
        const val LONG_FUEL_TRIM_1: Byte = 0x07
        const val TIMING_ADVANCE: Byte = 0x0E     // (A - 128) / 2
        const val O2_VOLTAGE_B1S1: Byte = 0x14
        const val RUN_TIME_SINCE_START: Byte = 0x1F
        const val SUPPORTED_PIDS_20_40: Byte = 0x20
        const val DISTANCE_WITH_MIL: Byte = 0x21
        const val FUEL_RAIL_PRESSURE: Byte = 0x22  // (256*A + B) * 0.079
        const val FUEL_RAIL_GAUGE_PRESSURE: Byte = 0x23  // (256*A + B) * 10
        const val O2_SENSOR_WIDE_B1S1: Byte = 0x24
        const val COMMANDED_EGR: Byte = 0x2C
        const val EVAP_PURGE: Byte = 0x2E
        const val FUEL_LEVEL: Byte = 0x2F
        const val WARMUPS_SINCE_CODES_CLEARED: Byte = 0x30
        const val DISTANCE_SINCE_CODES_CLEARED: Byte = 0x31
        const val EVAP_PRESSURE: Byte = 0x32
        const val CAT_TEMP_B1S1: Byte = 0x3C
    }
}
