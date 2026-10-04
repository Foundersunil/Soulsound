package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminAuditLog(
    val logId: String,
    val adminUserId: String,
    val adminEmail: String,
    val action: String, // CREATE, UPDATE, DELETE, RESTORE, SEED
    val resourceType: String, // FREQUENCY, CATEGORY, METADATA
    val resourceId: String,
    val resourceName: String,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestampMillis))
}
