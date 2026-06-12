package com.example.n54guru.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.n54guru.models.OBDLog

@Dao
interface OBDLogDao {
    @Insert
    suspend fun insert(obdLog: OBDLog)

    @Query("SELECT * FROM obd_logs ORDER BY timestamp DESC LIMIT 10080") // 7 days of data at 1-minute intervals
    suspend fun getRecentLogs(): List<OBDLog>

    @Query("DELETE FROM obd_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}
